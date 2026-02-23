package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a focus / Pomodoro session.
 */
data class FocusSession(
    val id: String,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long,
    val status: FocusStatus,
    val soundTrack: String?,
    val rating: Int?,
    val notes: String?,
    val createdAt: Instant
)

enum class FocusStatus {
    IN_PROGRESS, COMPLETED, CANCELLED, PAUSED
}
