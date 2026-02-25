package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Core habit entity representing a user's habit.
 * Supports daily, weekly, and custom frequency patterns.
 */
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], name = "index_habits_userId"),
        Index(value = ["syncState"], name = "index_habits_syncState"),
        Index(value = ["createdAt"], name = "index_habits_createdAt")
    ]
)
data class HabitEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val category: HabitCategory,
    val color: String, // Hex color code (e.g., "#6366F1")
    val icon: String, // Icon resource name or emoji
    val frequency: HabitFrequency,
    val customSchedule: List<DayOfWeek>? = null, // For CUSTOM frequency
    val reminderTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncState: SyncState = SyncState.PENDING
)

/**
 * Log entry for habit completion.
 * Records when a habit was completed, optional mood and notes.
 */
@Entity(
    tableName = "habit_logs",
    indices = [
        androidx.room.Index("habitId"),
        androidx.room.Index("completedAt")
    ]
)
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val userId: String,
    val completedAt: Instant,
    val mood: HabitMood? = null,
    val note: String? = null,
    val syncState: SyncState = SyncState.PENDING,
    val createdAt: Instant = Instant.now()
)

/**
 * Streak tracking for each habit.
 * Calculated and updated on each completion/skip.
 */
@Entity(tableName = "habit_streaks")
data class HabitStreakEntity(
    @PrimaryKey val habitId: String,
    val userId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null, // ISO date format: "2026-01-02"
    val totalCompletions: Int = 0,
    val updatedAt: Instant = Instant.now()
)

/**
 * Habit categories for organization and analytics.
 */
enum class HabitCategory {
    HEALTH,
    FITNESS,
    MINDFULNESS,
    PRODUCTIVITY,
    LEARNING,
    SOCIAL,
    CREATIVITY,
    FINANCE,
    OTHER;
    
    fun getDisplayName(): String = when (this) {
        HEALTH -> "Health"
        FITNESS -> "Fitness"
        MINDFULNESS -> "Mindfulness"
        PRODUCTIVITY -> "Productivity"
        LEARNING -> "Learning"
        SOCIAL -> "Social"
        CREATIVITY -> "Creativity"
        FINANCE -> "Finance"
        OTHER -> "Other"
    }
    
    fun getColor(): String = when (this) {
        HEALTH -> "#10B981" // Green
        FITNESS -> "#F59E0B" // Amber
        MINDFULNESS -> "#8B5CF6" // Purple
        PRODUCTIVITY -> "#3B82F6" // Blue
        LEARNING -> "#EC4899" // Pink
        SOCIAL -> "#14B8A6" // Teal
        CREATIVITY -> "#F97316" // Orange
        FINANCE -> "#22C55E" // Lime
        OTHER -> "#6B7280" // Gray
    }
}

/**
 * Frequency patterns for habit scheduling.
 */
enum class HabitFrequency {
    DAILY,
    WEEKLY,
    CUSTOM;
    
    fun getDisplayName(): String = when (this) {
        DAILY -> "Every day"
        WEEKLY -> "Once a week"
        CUSTOM -> "Custom schedule"
    }
}

/**
 * Optional mood tracking for habit completions.
 */
enum class HabitMood {
    GREAT,
    GOOD,
    OKAY,
    HARD,
    TERRIBLE;
    
    fun getEmoji(): String = when (this) {
        GREAT -> "😊"
        GOOD -> "🙂"
        OKAY -> "😐"
        HARD -> "😓"
        TERRIBLE -> "😞"
    }
    
    fun getDisplayName(): String = when (this) {
        GREAT -> "Great"
        GOOD -> "Good"
        OKAY -> "Okay"
        HARD -> "Hard"
        TERRIBLE -> "Terrible"
    }
}
