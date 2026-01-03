package com.ninety5.habitate.data.health

import java.time.Instant

data class HealthWorkout(
    val externalId: String,
    val type: String,
    val startTs: Instant,
    val endTs: Instant,
    val calories: Double?,
    val distanceMeters: Double?
)
