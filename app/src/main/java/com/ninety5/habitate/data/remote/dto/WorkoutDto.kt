package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class WorkoutDto(
    val id: String,
    val type: String,
    val source: String,
    @Json(name = "external_id") val externalId: String?,
    @Json(name = "start_ts") val startTs: Instant,
    @Json(name = "end_ts") val endTs: Instant,
    @Json(name = "distance_meters") val distanceMeters: Double?,
    val calories: Double?,
    @Json(name = "created_at") val createdAt: Instant,
    @Json(name = "updated_at") val updatedAt: Instant
)
