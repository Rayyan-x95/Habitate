package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val content: String?,
    val mediaUrl: String?,
    val status: MessageStatus, // SENDING, SENT, DELIVERED, READ, FAILED
    val createdAt: Long,
    val isDeleted: Boolean = false
)
