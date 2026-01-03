package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "post_id") val postId: String,
    val text: String,
    @Json(name = "created_at") val createdAt: Instant
)

