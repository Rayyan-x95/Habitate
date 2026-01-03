package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val source: WorkoutSource,        // MANUAL, HEALTH_CONNECT
    val externalId: String?,          // Health Connect ID for dedup
    val type: String,
    val startTs: Instant,
    val endTs: Instant,
    val distanceMeters: Double?,
    val calories: Double?,
    val isArchived: Boolean = false,
    val syncState: SyncState,
    val updatedAt: Instant
)
