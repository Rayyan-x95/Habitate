package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User entity representing a user in the local database.
 * Implements the canonical User data model.
 */
@Entity(
    tableName = "users",
    indices = [
        Index("username", unique = true),
        Index("email", unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey 
    val id: String,
    val username: String,
    val email: String?, // Nullable for users fetched without email from server
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val createdAt: Long = System.currentTimeMillis(),
    // passwordHash not stored locally for security - only on backend
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isOnline: Boolean = false,
    val isStealthMode: Boolean = false,
    val lastActive: Long = 0
)
