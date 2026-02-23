package com.ninety5.habitate.domain.model

import java.time.Instant

data class Conversation(
    val id: String,
    val name: String?,
    val avatarUrl: String? = null,
    val isGroup: Boolean = false,
    val isMuted: Boolean = false,
    val lastMessageText: String? = null,
    val lastMessageAt: Instant? = null,
    val participants: List<User> = emptyList(),
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val createdAt: Instant = Instant.EPOCH
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val mediaUrl: String? = null,
    val timestamp: Instant,
    val isRead: Boolean,
    val isDeleted: Boolean = false,
    val reactions: List<MessageReaction> = emptyList()
)

data class MessageReaction(
    val userId: String,
    val emoji: String
)

data class TypingEvent(
    val conversationId: String,
    val userId: String,
    val isTyping: Boolean
)
