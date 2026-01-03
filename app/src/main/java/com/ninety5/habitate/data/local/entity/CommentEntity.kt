package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Comment entity representing a user's comment on a post.
 * Implements the canonical Comment data model.
 */
@Entity(
    tableName = "comments",
    indices = [
        Index("userId"),
        Index("postId"),
        Index("createdAt"),
        Index("syncState")
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
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val postId: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.PENDING
)
