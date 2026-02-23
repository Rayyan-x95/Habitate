package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a workout.
 */
data class Workout(
    val id: String,
    val userId: String,
    val type: WorkoutType,
    val source: WorkoutSource,
    val externalId: String?,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long,
    val distanceMeters: Double?,
    val caloriesBurned: Int?,
    val heartRateAvg: Int?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Instant
)

enum class WorkoutType {
    RUNNING, WALKING, CYCLING, SWIMMING, STRENGTH,
    YOGA, HIIT, CARDIO, STRETCHING, OTHER
}

enum class WorkoutSource {
    MANUAL, HEALTH_CONNECT, IMPORTED
}
