package com.ninety5.habitate.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDate

/**
 * Habit with completion logs.
 */
data class HabitWithLogs(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val logs: List<HabitLogEntity>
) {
    /**
     * Check if habit is completed today.
     */
    val isCompletedToday: Boolean
        get() = logs.any { log ->
            val logDate = log.completedAt.toString().substringBefore('T')
            val today = LocalDate.now().toString()
            logDate == today
        }

    /**
     * Calculate completion rate (last 30 days).
     */
    val completionRate: Float
        get() {
            val thirtyDaysAgo = LocalDate.now().minusDays(30)
            val recentLogs = logs.filter { log ->
                val logDate = LocalDate.parse(log.completedAt.toString().substringBefore('T'))
                logDate.isAfter(thirtyDaysAgo)
            }
            return if (habit.frequency == HabitFrequency.DAILY) {
                (recentLogs.size / 30f).coerceAtMost(1f)
            } else {
                (recentLogs.size / 12f).coerceAtMost(1f) // ~4 weeks
            }
        }
}

/**
 * Habit with streak information.
 */
data class HabitWithStreak(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val streak: HabitStreakEntity?
)

/**
 * Complete habit details with logs and streak.
 */
data class HabitWithDetails(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val logs: List<HabitLogEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val streak: HabitStreakEntity?
) {
    val isCompletedToday: Boolean
        get() = logs.any { log ->
            val logDate = log.completedAt.toString().substringBefore('T')
            val today = LocalDate.now().toString()
            logDate == today
        }

    val completionRate: Float
        get() {
            val thirtyDaysAgo = LocalDate.now().minusDays(30)
            val recentLogs = logs.filter { log ->
                val logDate = LocalDate.parse(log.completedAt.toString().substringBefore('T'))
                logDate.isAfter(thirtyDaysAgo)
            }
            return if (habit.frequency == HabitFrequency.DAILY) {
                (recentLogs.size / 30f).coerceAtMost(1f)
            } else {
                (recentLogs.size / 12f).coerceAtMost(1f)
            }
        }
}
