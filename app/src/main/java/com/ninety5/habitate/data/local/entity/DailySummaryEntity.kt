package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val date: LocalDate,
    val steps: Int,
    val caloriesBurned: Double,
    val distanceMeters: Double,
    val activeMinutes: Int,
    val mood: String? = null, // e.g., "Happy", "Stressed", "Energetic"
    val notes: String? = null
)
