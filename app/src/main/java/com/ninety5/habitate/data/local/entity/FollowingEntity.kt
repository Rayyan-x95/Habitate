package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Follow entity representing a follower-following relationship between two users.
 * Implements the canonical Follow data model with composite key.
 * 
 * Primary key is composite (followerId + followingId) to ensure one follow relationship per pair.
 */
@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followingId"],
    indices = [
        Index("followerId"),
        Index("followingId"),
        Index("syncState"),
        Index("createdAt")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FollowEntity(
    val followerId: String,  // User who is following
    val followingId: String, // User being followed
    val createdAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.PENDING
)
