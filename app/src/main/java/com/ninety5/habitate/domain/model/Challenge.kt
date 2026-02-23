package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a challenge.
 */
data class Challenge(
    val id: String,
    val title: String,
    val description: String?,
    val habitatId: String?,
    val creatorId: String?,
    val metricType: ChallengeMetric,
    val targetValue: Double,
    val startDate: Instant,
    val endDate: Instant,
    val participantCount: Int,
    val isJoined: Boolean,
    val createdAt: Instant
)

data class ChallengeProgress(
    val challengeId: String,
    val userId: String,
    val currentValue: Double,
    val status: ChallengeStatus,
    val rank: Int?
)

enum class ChallengeMetric {
    STEPS, WORKOUTS, HABITS_COMPLETED, FOCUS_MINUTES,
    POSTS, STREAK_DAYS, CUSTOM
}

enum class ChallengeStatus {
    ACTIVE, COMPLETED, FAILED, WITHDRAWN
}
