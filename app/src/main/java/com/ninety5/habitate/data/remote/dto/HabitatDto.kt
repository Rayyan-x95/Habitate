package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class HabitatDto(
    val id: String,
    val name: String,
    val description: String?,
    @Json(name = "privacy_level") val privacyLevel: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "created_at") val createdAt: Instant,
    @Json(name = "updated_at") val updatedAt: Instant
)
