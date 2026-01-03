package com.ninety5.habitate.domain.model

import java.time.Instant

data class Conversation(
    val id: String,
    val participants: List<User>,
    val lastMessage: Message?,
    val unreadCount: Int
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Instant,
    val isRead: Boolean
)
