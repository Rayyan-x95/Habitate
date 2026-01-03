package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class JournalEntryDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val title: String?,
    val content: String,
    val mood: String?,
    val tags: String, // JSON array string
    @Json(name = "is_private") val isPrivate: Boolean,
    @Json(name = "created_at") val createdAt: Instant,
    @Json(name = "updated_at") val updatedAt: Instant
)

@JsonClass(generateAdapter = true)
data class JournalEntryCreateRequest(
    val title: String?,
    val content: String,
    val mood: String?,
    val tags: String = "[]",
    @Json(name = "is_private") val isPrivate: Boolean = true
)
