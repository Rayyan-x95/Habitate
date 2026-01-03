package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val targetId: String?,
    val isRead: Boolean,
    val isDigest: Boolean = false,
    val createdAt: Instant
)
