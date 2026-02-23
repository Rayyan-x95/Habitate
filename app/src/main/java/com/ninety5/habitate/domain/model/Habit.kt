package com.ninety5.habitate.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Domain model for a habit.
 */
data class Habit(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val category: HabitCategory,
    val color: String,
    val icon: String,
    val frequency: HabitFrequency,
    val customSchedule: List<DayOfWeek>,
    val reminderTime: LocalTime?,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class HabitWithDetails(
    val habit: Habit,
    val streak: HabitStreak,
    val recentLogs: List<HabitLog>
)

data class HabitStreak(
    val habitId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val lastCompletedAt: Instant?
)

data class HabitLog(
    val id: String,
    val habitId: String,
    val completedAt: Instant,
    val mood: HabitMood?,
    val note: String?
)

enum class HabitCategory {
    HEALTH, FITNESS, MINDFULNESS, PRODUCTIVITY, LEARNING,
    SOCIAL, CREATIVITY, FINANCE, CUSTOM;

    fun getDisplayName(): String = when (this) {
        HEALTH -> "Health"
        FITNESS -> "Fitness"
        MINDFULNESS -> "Mindfulness"
        PRODUCTIVITY -> "Productivity"
        LEARNING -> "Learning"
        SOCIAL -> "Social"
        CREATIVITY -> "Creativity"
        FINANCE -> "Finance"
        CUSTOM -> "Other"
    }

    fun getColor(): String = when (this) {
        HEALTH -> "#10B981"
        FITNESS -> "#F59E0B"
        MINDFULNESS -> "#8B5CF6"
        PRODUCTIVITY -> "#3B82F6"
        LEARNING -> "#EC4899"
        SOCIAL -> "#14B8A6"
        CREATIVITY -> "#F97316"
        FINANCE -> "#22C55E"
        CUSTOM -> "#6B7280"
    }
}

enum class HabitFrequency {
    DAILY, WEEKLY, CUSTOM;

    fun getDisplayName(): String = when (this) {
        DAILY -> "Every day"
        WEEKLY -> "Once a week"
        CUSTOM -> "Custom schedule"
    }
}

enum class HabitMood {
    GREAT, GOOD, NEUTRAL, BAD, TERRIBLE;

    fun getEmoji(): String = when (this) {
        GREAT -> "😊"
        GOOD -> "🙂"
        NEUTRAL -> "😐"
        BAD -> "😓"
        TERRIBLE -> "😰"
    }

    fun getDisplayName(): String = when (this) {
        GREAT -> "Great"
        GOOD -> "Good"
        NEUTRAL -> "Okay"
        BAD -> "Hard"
        TERRIBLE -> "Terrible"
    }
}
