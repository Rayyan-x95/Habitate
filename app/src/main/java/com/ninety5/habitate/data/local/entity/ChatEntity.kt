package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: ChatType, // DIRECT, HABITAT
    val title: String?,
    val lastMessage: String?,
    val updatedAt: Long,
    val isMuted: Boolean = false
)
