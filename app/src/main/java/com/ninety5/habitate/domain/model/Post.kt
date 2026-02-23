package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a social post.
 * Independent of Room entities and API DTOs.
 */
data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val contentText: String,
    val mediaUrls: List<String>,
    val visibility: PostVisibility,
    val habitatId: String?,
    val workoutId: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class PostVisibility {
    PUBLIC,
    FOLLOWERS_ONLY,
    HABITAT_ONLY,
    PRIVATE
}
