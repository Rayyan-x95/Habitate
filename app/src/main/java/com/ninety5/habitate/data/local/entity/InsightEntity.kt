package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class InsightType {
    STREAK_RISK,
    PATTERN_DETECTED,
    SUGGESTION,
    MILESTONE_APPROACHING,
    MOOD_CORRELATION,
    WEEKLY_SUMMARY,
    TASK_FAILURE,
    HABIT_FRICTION,
    ENERGY_TREND
}

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH
}

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val priority: InsightPriority,
    val relatedEntityId: String?,
    val createdAt: Instant,
    val isDismissed: Boolean = false
)
