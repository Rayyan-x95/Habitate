package com.ninety5.habitate.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ninety5.habitate.data.local.entity.HabitEntity
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitStreakEntity

/**
 * Habit with all its completion logs.
 */
data class HabitWithLogs(
    @Embedded val habit: HabitEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val logs: List<HabitLogEntity>
)

/**
 * Habit with its current streak information.
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
 * Complete habit data with logs and streak.
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
        get() {
            val today = java.time.LocalDate.now().toString()
            return logs.any { log ->
                val logDate = java.time.Instant.ofEpochMilli(log.completedAt.toEpochMilli())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
                logDate == today
            }
        }
    
    val completionRate: Float
        get() {
            if (logs.isEmpty()) return 0f
            val daysSinceCreation = java.time.Duration.between(
                habit.createdAt,
                java.time.Instant.now()
            ).toDays().toInt().coerceAtLeast(1)
            
            return logs.size.toFloat() / daysSinceCreation
        }
}
