package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val metricType: String,
    val targetValue: Double,
    val startDate: Instant,
    val endDate: Instant,
    val creatorId: String?,
    val habitatId: String?,
    val syncState: SyncState,
    val createdAt: Instant
)
