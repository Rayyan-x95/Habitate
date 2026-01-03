package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class StoryDto(
    val id: String,
    @Json(name = "author_id") val authorId: String,
    @Json(name = "media_uri") val mediaUri: String,
    val caption: String?,
    val visibility: String = "PUBLIC",
    @Json(name = "created_at") val createdAt: Instant,
    @Json(name = "expires_at") val expiresAt: Instant
)
