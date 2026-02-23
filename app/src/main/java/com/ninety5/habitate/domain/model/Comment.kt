package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a comment with author info (flattened from CommentWithUser).
 */
data class Comment(
    val id: String,
    val userId: String,
    val postId: String,
    val text: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String?,
    val createdAt: Instant
)
