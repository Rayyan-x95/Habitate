package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Like entity representing a user's like on a post.
 * Implements the canonical Like data model.
 * 
 * Primary key is composite (userId + postId) to ensure one like per user per post.
 */
@Entity(
    tableName = "likes",
    primaryKeys = ["userId", "postId"],
    indices = [
        Index("userId"),
        Index("postId"),
        Index("syncState"),
        Index("createdAt")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LikeEntity(
    val userId: String,
    val postId: String,
    val reactionType: String = "HEART",
    val createdAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.PENDING
)
