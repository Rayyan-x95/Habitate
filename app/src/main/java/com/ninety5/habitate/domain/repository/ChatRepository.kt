package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Conversation
import com.ninety5.habitate.domain.model.Message
import com.ninety5.habitate.domain.model.TypingEvent
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for chat/messaging operations.
 */
interface ChatRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<Message>>
    fun observeTypingEvents(): Flow<TypingEvent>
    suspend fun sendMessage(conversationId: String, content: String, mediaUrl: String?): AppResult<Message>
    suspend fun deleteMessage(messageId: String): AppResult<Unit>
    suspend fun addReaction(messageId: String, emoji: String): AppResult<Unit>
    suspend fun removeReaction(messageId: String, emoji: String): AppResult<Unit>
    suspend fun muteConversation(conversationId: String, muted: Boolean): AppResult<Unit>
    suspend fun sendTyping(conversationId: String, isTyping: Boolean)
    suspend fun refreshConversations(): AppResult<Unit>
    suspend fun refreshMessages(conversationId: String): AppResult<Unit>
    suspend fun initializeRealtime(): AppResult<Unit>
    suspend fun disconnectRealtime()
}
