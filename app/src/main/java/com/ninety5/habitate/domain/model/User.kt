package com.ninety5.habitate.domain.model

/**
 * Domain model representing a user profile.
 * Framework-free — no Room/Moshi annotations.
 *
 * Note: [email] is internal to avoid leaking PII through the domain layer.
 * Use DTOs or repository methods for email-dependent operations.
 */
data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val bio: String? = null,
    internal val email: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isOnline: Boolean = false,
    val isStealthMode: Boolean = false,
    val lastActive: Long = 0,
    val createdAt: Long = 0
) {
    /** Redact email from toString() to protect PII. */
    override fun toString(): String =
        "User(id=$id, displayName=$displayName, username=$username, avatarUrl=$avatarUrl, bio=$bio, followerCount=$followerCount, followingCount=$followingCount, postCount=$postCount, isOnline=$isOnline, isStealthMode=$isStealthMode)"
}
