package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a story.
 */
data class Story(
    val id: String,
    val userId: String,
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val mediaUrl: String,
    val caption: String?,
    val visibility: PostVisibility,
    val viewCount: Int,
    val isSaved: Boolean,
    val createdAt: Instant,
    val expiresAt: Instant
)
