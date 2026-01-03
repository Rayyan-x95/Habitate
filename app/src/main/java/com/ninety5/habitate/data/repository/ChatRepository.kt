package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.ChatDao
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.dao.MessageReactionDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.MessageReactionEntity
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.RealtimeClient
import com.ninety5.habitate.data.remote.WsMessage
import com.ninety5.habitate.data.remote.PresenceStatus
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val messageReactionDao: MessageReactionDao,
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val realtimeClient: RealtimeClient,
    private val syncQueueDao: SyncQueueDao,
    private val moshi: Moshi
) {
    val chats: Flow<List<ChatEntity>> = chatDao.getChats()

    private val _typingEvents = MutableSharedFlow<WsMessage.Typing>()
    val typingEvents = _typingEvents.asSharedFlow()

    fun getMessages(chatId: String): Flow<List<MessageWithReactions>> = messageDao.getMessages(chatId)

    suspend fun initializeRealtime() {
        try {
            realtimeClient.connect()
            realtimeClient.messages.collect { msg ->
                when (msg) {
                    is WsMessage.NewMessage -> {
                        val entity = MessageEntity(
                            id = msg.message.id,
                            chatId = msg.message.conversationId,
                            senderId = msg.message.senderId,
                            content = msg.message.content,
                            mediaUrl = null, // Domain model doesn't have mediaUrl yet
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
                        _typingEvents.emit(msg)
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
                            messageReactionDao.removeReaction(msg.messageId, msg.userId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "ChatRepository: Failed to initialize realtime connection")
        }
    }

    suspend fun muteChat(chatId: String, isMuted: Boolean) {
        chatDao.updateMuteState(chatId, isMuted)
        
        // Sync mute state to server
        val syncOp = SyncOperationEntity(
            entityType = "chat_mute",
            entityId = chatId,
            operation = "UPDATE",
            payload = """{"chatId":"$chatId","isMuted":$isMuted}""",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
        // Sync to server
        val syncOp = SyncOperationEntity(
            entityType = "message",
            entityId = messageId,
            operation = "DELETE",
            payload = "",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun markAsRead(messageId: String) {
        messageDao.updateStatus(messageId, MessageStatus.READ)
        // In a real app, we would sync this to server too
    }

    suspend fun addReaction(messageId: String, userId: String, emoji: String) {
        val reaction = MessageReactionEntity(
            messageId = messageId,
            userId = userId,
            emoji = emoji
        )
        messageReactionDao.upsert(reaction)
        
        // Sync reaction to server
        val syncOp = SyncOperationEntity(
            entityType = "message_reaction",
            entityId = "${messageId}_${userId}",
            operation = "CREATE",
            payload = """{"messageId":"$messageId","userId":"$userId","emoji":"$emoji"}""",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun removeReaction(messageId: String, userId: String) {
        messageReactionDao.removeReaction(messageId, userId)
        
        // Sync reaction removal to server
        val syncOp = SyncOperationEntity(
            entityType = "message_reaction",
            entityId = "${messageId}_${userId}",
            operation = "DELETE",
            payload = """{"messageId":"$messageId","userId":"$userId"}""",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun sendTyping(chatId: String, isTyping: Boolean) {
        // Send via WebSocket - no persistence needed for ephemeral typing indicators
        realtimeClient.sendMessage("""{"type":"TYPING","chatId":"$chatId","isTyping":$isTyping}""")
    }

    suspend fun markMessagesAsRead(chatId: String) {
        messageDao.markAllAsRead(chatId)
        try {
            apiService.markChatRead(chatId)
        } catch (e: Exception) {
            Timber.d(e, "ChatRepository: Failed to sync read status for chat $chatId (offline)")
        }
    }

    suspend fun refreshChats() {
        try {
            val remoteChats = apiService.getChats()
            remoteChats.forEach { dto ->
                chatDao.upsert(
                    ChatEntity(
                        id = dto.id,
                        type = dto.type,
                        title = dto.title,
                        lastMessage = dto.lastMessage,
                        updatedAt = dto.updatedAt
                    )
                )
            }
        } catch (e: Exception) {
            Timber.d(e, "ChatRepository: Failed to refresh chats (offline)")
        }
    }

    suspend fun refreshMessages(chatId: String) {
        try {
            val remoteMessages = apiService.getMessages(chatId, null)
            remoteMessages.forEach { dto ->
                messageDao.upsert(
                    MessageEntity(
                        id = dto.id,
                        chatId = dto.chatId,
                        senderId = dto.senderId,
                        content = dto.content,
                        mediaUrl = dto.mediaUrl,
                        status = dto.status,
                        createdAt = dto.createdAt
                    )
                )
            }
        } catch (e: Exception) {
            Timber.d(e, "ChatRepository: Failed to refresh messages (offline)")
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

        // 1. Local Write
        messageDao.upsert(message)

        // 2. Queue Sync
        val dto = MessageDto(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            mediaUrl = mediaUri, // In real app, this would be S3 URL after upload
            status = MessageStatus.SENDING,
            createdAt = message.createdAt
        )
        
        val payload = moshi.adapter(MessageDto::class.java).toJson(dto)
        
        syncQueueDao.insert(
            SyncOperationEntity(
                entityType = "message",
                entityId = messageId,
                operation = "CREATE",
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
        )
    }
}
