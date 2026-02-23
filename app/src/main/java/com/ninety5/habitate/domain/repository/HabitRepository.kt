package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Habit
import com.ninety5.habitate.domain.model.HabitLog
import com.ninety5.habitate.domain.model.HabitMood
import com.ninety5.habitate.domain.model.HabitStreak
import com.ninety5.habitate.domain.model.HabitWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for habit operations.
 * All methods return domain [Habit] / [HabitWithDetails] models, never framework entities.
 */
interface HabitRepository {

    // ── Observe ────────────────────────────────────────────────────────
    /** All habits (active + archived) for current user. */
    fun observeAllHabits(): Flow<List<Habit>>

    /** Only non-archived habits. */
    fun observeActiveHabits(): Flow<List<Habit>>

    /** Active habits with streak info (for list screen). */
    fun observeActiveHabitsWithStreaks(): Flow<List<HabitWithDetails>>

    /** Single habit with full logs + streak (detail screen). */
    fun observeHabitWithDetails(habitId: String): Flow<HabitWithDetails?>

    /** Streak for a specific habit. */
    fun observeStreak(habitId: String): Flow<HabitStreak?>

    /** Whether the habit was completed today. */
    fun isCompletedToday(habitId: String): Flow<Boolean>

    // ── One-shot ───────────────────────────────────────────────────────
    suspend fun getHabit(habitId: String): AppResult<Habit>

    // ── Mutations ──────────────────────────────────────────────────────
    suspend fun createHabit(habit: Habit): AppResult<Habit>
    suspend fun updateHabit(habit: Habit): AppResult<Unit>
    suspend fun deleteHabit(habitId: String): AppResult<Unit>
    suspend fun archiveHabit(habitId: String): AppResult<Unit>

    // ── Completion tracking ────────────────────────────────────────────
    /** Mark habit completed (today). Returns the log entry created. */
    suspend fun logCompletion(habitId: String, mood: HabitMood? = null, note: String? = null): AppResult<HabitLog>

    /** Undo a completion for a specific date. */
    suspend fun undoCompletion(habitId: String, date: String): AppResult<Unit>

    /** Recent completion history. */
    suspend fun getCompletionHistory(habitId: String, limit: Int = 30): AppResult<List<HabitLog>>

    // ── Sync ───────────────────────────────────────────────────────────
    suspend fun syncHabits(): AppResult<Unit>
}
