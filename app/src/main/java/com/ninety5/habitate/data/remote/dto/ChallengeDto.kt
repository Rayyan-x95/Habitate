package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class ChallengeDto(
    val id: String,
    val title: String,
    val description: String?,
    @Json(name = "metric_type") val metricType: String,
    @Json(name = "target_value") val targetValue: Double,
    @Json(name = "start_date") val startDate: Instant,
    @Json(name = "end_date") val endDate: Instant,
    @Json(name = "creator_id") val creatorId: String,
    @Json(name = "habitat_id") val habitatId: String?,
    @Json(name = "created_at") val createdAt: Instant
)

@JsonClass(generateAdapter = true)
data class ChallengeCreateRequest(
    val title: String,
    val description: String?,
    @Json(name = "metric_type") val metricType: String,
    @Json(name = "target_value") val targetValue: Double,
    @Json(name = "start_date") val startDate: Instant,
    @Json(name = "end_date") val endDate: Instant,
    @Json(name = "habitat_id") val habitatId: String? = null
)

@JsonClass(generateAdapter = true)
data class ChallengeParticipantDto(
    val id: String,
    @Json(name = "challenge_id") val challengeId: String,
    @Json(name = "user_id") val userId: String,
    val progress: Double,
    @Json(name = "joined_at") val joinedAt: Instant
)

@JsonClass(generateAdapter = true)
data class LeaderboardEntryDto(
    val rank: Int,
    @Json(name = "user_id") val userId: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    val score: Double
)
