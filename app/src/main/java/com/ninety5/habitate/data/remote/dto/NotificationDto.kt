package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val title: String,
    val body: String,
    val type: String,
    @Json(name = "target_id") val targetId: String?,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: Instant
)
