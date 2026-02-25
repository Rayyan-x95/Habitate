package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.ChatDao
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.dao.MessageReactionDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.MessageReactionEntity
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.PresenceStatus
import com.ninety5.habitate.data.remote.RealtimeClient
import com.ninety5.habitate.data.remote.WsMessage
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.Conversation
import com.ninety5.habitate.domain.model.Message
import com.ninety5.habitate.domain.model.TypingEvent
import com.ninety5.habitate.domain.repository.ChatRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [ChatRepository].
 *
 * Handles:
 * - Conversation and message observation
 * - Message sending, deletion, and reactions
 * - Realtime WebSocket connection with exponential backoff
 * - Chat muting
 * - Offline-first with sync queue
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val messageReactionDao: MessageReactionDao,
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val realtimeClient: RealtimeClient,
    private val syncQueueDao: SyncQueueDao,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi
) : ChatRepository {

    // ── Realtime state ─────────────────────────────────────────────────

    private val _typingEvents = MutableSharedFlow<TypingEvent>()
    val typingEvents: SharedFlow<TypingEvent> = _typingEvents.asSharedFlow()

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var realtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var realtimeJob: Job? = null
    private var reconnectAttempt = 0
    private val maxReconnectAttempts = 5
    private val baseReconnectDelayMs = 1000L

    // ══════════════════════════════════════════════════════════════════════
    // DOMAIN INTERFACE METHODS
    // ══════════════════════════════════════════════════════════════════════

    override fun observeConversations(): Flow<List<Conversation>> {
        return chatDao.getChats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessages(conversationId).map { messagesWithReactions ->
            messagesWithReactions.map { it.toDomain() }
        }
    }

    override fun observeTypingEvents(): Flow<TypingEvent> = typingEvents

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        mediaUrl: String?
    ): AppResult<Message> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val messageId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val entity = MessageEntity(
                id = messageId,
                chatId = conversationId,
                senderId = userId,
                content = content,
                mediaUrl = mediaUrl,
                status = MessageStatus.SENDING,
                createdAt = now
            )
            messageDao.upsert(entity)

            val dto = MessageDto(
                id = messageId,
                chatId = conversationId,
                senderId = userId,
                content = content,
                mediaUrl = mediaUrl,
                status = MessageStatus.SENDING,
                createdAt = now
            )
            queueSync("chat_message", messageId, "CREATE",
                moshi.adapter(MessageDto::class.java).toJson(dto))

            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            AppResult.Error(AppError.Database(e.message ?: "Failed to send message"))
        }
    }

    override suspend fun deleteMessage(messageId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Verify ownership before deleting
            val message = messageDao.getMessageById(messageId)
            if (message == null || message.senderId != userId) {
                return AppResult.Error(AppError.Unauthorized("Cannot delete another user's message"))
            }

            messageDao.deleteMessage(messageId) // Soft-delete: sets isDeleted = 1
            queueSync("chat_message", messageId, "DELETE", "{}")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete message: $messageId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to delete message"))
        }
    }

    override suspend fun addReaction(messageId: String, emoji: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val reaction = MessageReactionEntity(
                messageId = messageId,
                userId = userId,
                emoji = emoji
            )
            messageReactionDao.upsert(reaction)

            queueSync("message_reaction", "${messageId}_${userId}", "CREATE",
                """{"messageId":"$messageId","userId":"$userId","emoji":"$emoji"}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to add reaction to message: $messageId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to add reaction"))
        }
    }

    override suspend fun removeReaction(messageId: String, emoji: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            messageReactionDao.removeReactionByEmoji(messageId, userId, emoji)

            queueSync("message_reaction", "${messageId}_${userId}", "DELETE",
                """{"messageId":"$messageId","userId":"$userId","emoji":"$emoji"}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove reaction from message: $messageId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to remove reaction"))
        }
    }

    override suspend fun muteConversation(conversationId: String, muted: Boolean): AppResult<Unit> {
        return try {
            chatDao.updateMuteState(conversationId, muted)
            queueSync("chat_mute", conversationId, "UPDATE",
                """{"chatId":"$conversationId","isMuted":$muted}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to mute conversation: $conversationId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to mute conversation"))
        }
    }

    override suspend fun initializeRealtime(): AppResult<Unit> {
        return try {
            realtimeJob?.cancel()
            reconnectAttempt = 0

            realtimeJob = realtimeScope.launch {
                while (isActive) {
                    try {
                        _connectionState.value = if (reconnectAttempt == 0)
                            ConnectionState.CONNECTING else ConnectionState.RECONNECTING
                        realtimeClient.connect()
                        _connectionState.value = ConnectionState.CONNECTED
                        reconnectAttempt = 0

                        realtimeClient.messages.collect { msg ->
                            handleWsMessage(msg)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Timber.e(e, "Realtime connection failed (attempt $reconnectAttempt)")
                        _connectionState.value = ConnectionState.DISCONNECTED

                        if (reconnectAttempt < maxReconnectAttempts) {
                            val delayMs = baseReconnectDelayMs * (1L shl reconnectAttempt)
                            reconnectAttempt++
                            delay(delayMs)
                        } else {
                            Timber.e("Max reconnection attempts reached")
                            break
                        }
                    }
                }
            }

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize realtime")
            AppResult.Error(AppError.Network(e.message ?: "Failed to initialize realtime"))
        }
    }

    override suspend fun disconnectRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
        reconnectAttempt = 0
        _connectionState.value = ConnectionState.DISCONNECTED
        realtimeClient.close()
        Timber.d("Realtime disconnected")
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEGACY VIEWMODEL-FACING METHODS
    // These support existing ViewModels that haven't migrated to domain interface.
    // ══════════════════════════════════════════════════════════════════════

    val chats: Flow<List<ChatEntity>> = chatDao.getChats()

    fun getMessages(chatId: String): Flow<List<MessageWithReactions>> =
        messageDao.getMessages(chatId)

    fun initializeRealtimeLegacy() {
        realtimeScope.launch {
            initializeRealtime()
        }
    }

    fun disconnectRealtimeLegacy() {
        realtimeJob?.cancel()
        realtimeJob = null
        reconnectAttempt = 0
        _connectionState.value = ConnectionState.DISCONNECTED
        realtimeClient.close()
    }

    /**
     * Clean up all resources. Call from Application.onTerminate().
     */
    fun destroy() {
        realtimeScope.cancel()
        realtimeClient.close()
        realtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        realtimeJob = null
        reconnectAttempt = 0
        _connectionState.value = ConnectionState.DISCONNECTED
        Timber.d("ChatRepositoryImpl: Destroyed")
    }

    suspend fun muteChat(chatId: String, isMuted: Boolean) {
        muteConversation(chatId, isMuted)
    }

    suspend fun deleteMessageLegacy(messageId: String) {
        val userId = securePreferences.userId ?: return
        val message = messageDao.getMessageById(messageId)
        if (message == null || message.senderId != userId) return
        messageDao.deleteMessage(messageId)
        queueSync("chat_message", messageId, "DELETE", "{}")
    }

    suspend fun addReaction(messageId: String, userId: String, emoji: String) {
        val reaction = MessageReactionEntity(
            messageId = messageId,
            userId = userId,
            emoji = emoji
        )
        messageReactionDao.upsert(reaction)
        queueSync("message_reaction", "${messageId}_${userId}", "CREATE",
            """{"messageId":"$messageId","userId":"$userId","emoji":"$emoji"}""")
    }

    suspend fun removeReactionLegacy(messageId: String, userId: String) {
        messageReactionDao.removeReaction(messageId, userId)
        queueSync("message_reaction", "${messageId}_${userId}", "DELETE",
            """{"messageId":"$messageId","userId":"$userId"}""")
    }

    override suspend fun sendTyping(conversationId: String, isTyping: Boolean) {
        realtimeClient.sendMessage("""{ "type":"TYPING","chatId":"$conversationId","isTyping":$isTyping}""")
    }

    suspend fun markMessagesAsRead(chatId: String) {
        messageDao.markAllAsRead(chatId)
        try {
            apiService.markChatRead(chatId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Failed to sync read status (offline)")
        }
    }

    override suspend fun refreshConversations(): AppResult<Unit> {
        return try {
            val remoteChats = apiService.getChats()
            remoteChats.forEach { dto -> chatDao.upsert(dto.toEntity()) }
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Failed to refresh chats (offline)")
            AppResult.Error(AppError.Network(e.message ?: "Failed to refresh conversations"))
        }
    }

    override suspend fun refreshMessages(conversationId: String): AppResult<Unit> {
        return try {
            val remoteMessages = apiService.getMessages(conversationId, null)
            remoteMessages.forEach { dto -> messageDao.upsert(dto.toEntity()) }
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Failed to refresh messages (offline)")
            AppResult.Error(AppError.Network(e.message ?: "Failed to refresh messages"))
        }
    }

    suspend fun sendMessage(chatId: String, content: String?, senderId: String, mediaUri: String? = null) {
        val messageId = UUID.randomUUID().toString()
        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            mediaUrl = mediaUri,
            status = MessageStatus.SENDING,
            createdAt = System.currentTimeMillis()
        )

        messageDao.upsert(message)

        val dto = MessageDto(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            mediaUrl = mediaUri,
            status = MessageStatus.SENDING,
            createdAt = message.createdAt
        )
        queueSync("chat_message", messageId, "CREATE",
            moshi.adapter(MessageDto::class.java).toJson(dto))
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun handleWsMessage(msg: WsMessage) {
        when (msg) {
            is WsMessage.NewMessage -> {
                val entity = MessageEntity(
                    id = msg.message.id,
                    chatId = msg.message.conversationId,
                    senderId = msg.message.senderId,
                    content = msg.message.content,
                    mediaUrl = null,
                    status = MessageStatus.DELIVERED,
                    createdAt = msg.message.timestamp.toEpochMilli()
                )
                messageDao.upsert(entity)
            }
            is WsMessage.Presence -> {
                val isOnline = msg.status == PresenceStatus.ONLINE
                userDao.updatePresence(msg.userId, isOnline, System.currentTimeMillis())
            }
            is WsMessage.Typing -> {
                _typingEvents.emit(
                    TypingEvent(
                        conversationId = msg.chatId,
                        userId = msg.userId,
                        isTyping = msg.isTyping
                    )
                )
            }
            is WsMessage.Reaction -> {
                if (msg.action == "ADD") {
                    messageReactionDao.upsert(
                        MessageReactionEntity(
                            messageId = msg.messageId,
                            userId = msg.userId,
                            emoji = msg.emoji
                        )
                    )
                } else {
                    messageReactionDao.removeReactionByEmoji(msg.messageId, msg.userId, msg.emoji)
                }
            }
        }
    }

    private suspend fun queueSync(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String
    ) {
        syncQueueDao.insert(
            SyncOperationEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
        )
    }
}
