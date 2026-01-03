package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long,
    val status: FocusSessionStatus, // COMPLETED, ABORTED, IN_PROGRESS
    val soundTrack: String? = null,
    val rating: Int? = null,
    val syncState: SyncState,
    val updatedAt: Instant
)

enum class FocusSessionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABORTED
}
