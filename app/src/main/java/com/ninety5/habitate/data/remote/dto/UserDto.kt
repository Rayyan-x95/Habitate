package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_stealth_mode") val isStealthMode: Boolean = false,
    @Json(name = "created_at") val createdAt: String
)
