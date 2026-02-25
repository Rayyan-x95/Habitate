package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatId"], name = "index_messages_chatId"),
        Index(value = ["senderId"], name = "index_messages_senderId")
    ]
)
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
