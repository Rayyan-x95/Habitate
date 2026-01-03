package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class PostDto(
    val id: String,
    val author: UserDto,
    @Json(name = "content_text") val contentText: String?,
    @Json(name = "media_uris") val mediaUris: String,
    val visibility: String,
    @Json(name = "habitat_id") val habitatId: String?,
    @Json(name = "workout_id") val workoutId: String?,
    @Json(name = "likes_count") val likesCount: Int,
    @Json(name = "comments_count") val commentsCount: Int,
    @Json(name = "shares_count") val sharesCount: Int,
    @Json(name = "is_liked") val isLiked: Boolean,
    @Json(name = "created_at") val createdAt: Instant,
    @Json(name = "updated_at") val updatedAt: Instant
)
