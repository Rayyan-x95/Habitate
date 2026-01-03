package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "challenge_progress")
data class ChallengeProgressEntity(
    @PrimaryKey val id: String, // Composite key usually, but simple ID for now
    val challengeId: String,
    val userId: String,
    val progress: Double,
    val status: ChallengeStatus, // JOINED, COMPLETED, FAILED
    val joinedAt: Instant,
    val updatedAt: Instant,
    val syncState: SyncState
)

enum class ChallengeStatus {
    JOINED, COMPLETED, FAILED
}
