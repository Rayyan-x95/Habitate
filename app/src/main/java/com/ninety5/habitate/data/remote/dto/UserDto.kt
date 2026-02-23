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
    val bio: String? = null,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_stealth_mode") val isStealthMode: Boolean = false,
    @Json(name = "follower_count") val followerCount: Int = 0,
    @Json(name = "following_count") val followingCount: Int = 0,
    @Json(name = "post_count") val postCount: Int = 0,
    @Json(name = "created_at") val createdAt: String
)
