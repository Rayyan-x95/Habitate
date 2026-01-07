package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.HabitLogDao
import com.ninety5.habitate.data.local.dao.HabitStreakDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.*
import com.ninety5.habitate.data.local.relation.HabitWithDetails
import com.ninety5.habitate.data.local.relation.HabitWithLogs
import com.ninety5.habitate.data.local.relation.HabitWithStreak
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.HabitDto
import com.ninety5.habitate.data.remote.dto.HabitLogDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for habit management.
 * 
 * Handles:
 * - CRUD operations for habits
 * - Completion logging with mood tracking
 * - Streak calculation and maintenance
 * - Offline-first data syncing
 * - Analytics data aggregation
 * - Glyph visual feedback (Nothing phones)
 */
@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitStreakDao: HabitStreakDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi,
    private val glyphManager: com.ninety5.habitate.core.glyph.HabitateGlyphManager
) {

    // ======================
    // HABIT QUERIES
    // ======================

    /**
     * Get all active habits for current user.
     */
    fun getActiveHabits(): Flow<List<HabitEntity>> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return habitDao.getActiveHabits(userId)
    }

    /**
     * Get habits with their streaks.
     */
    fun getActiveHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return habitDao.getActiveHabitsWithStreaks(userId)
    }

    /**
     * Get single habit with logs.
     */
    fun getHabitWithLogs(habitId: String): Flow<HabitWithLogs?> {
        return habitDao.getHabitWithLogs(habitId)
    }

    /**
     * Get habits with reminders enabled.
     */
    fun getHabitsWithReminders(): Flow<List<HabitEntity>> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return habitDao.getHabitsWithReminders(userId)
    }

    /**
     * Get habits by category.
     */
    fun getHabitsByCategory(category: HabitCategory): Flow<List<HabitEntity>> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return habitDao.getHabitsByCategory(category.name, userId)
    }

    // ======================
    // HABIT CRUD
    // ======================

    /**
     * Create a new habit (optimistic update).
     */
    suspend fun createHabit(
        title: String,
        description: String?,
        category: HabitCategory,
        color: String,
        icon: String,
        frequency: HabitFrequency,
        customSchedule: List<java.time.DayOfWeek>? = null,
        reminderTime: java.time.LocalTime? = null,
        reminderEnabled: Boolean = false
    ): Result<HabitEntity> {
        val userId = securePreferences.userId ?: return Result.failure(Exception("User not logged in"))

        val habit = HabitEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            category = category,
            color = color,
            icon = icon,
            frequency = frequency,
            customSchedule = customSchedule,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            isArchived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            syncState = SyncState.PENDING
        )

        return try {
            habitDao.upsert(habit)
            
            // Initialize streak
            habitStreakDao.upsert(HabitStreakEntity(
                habitId = habit.id,
                userId = userId
            ))
            
            // Queue for sync
            queueSync("habit", habit.id, "CREATE", moshi.adapter(HabitEntity::class.java).toJson(habit))
            
            Timber.d("Created habit: ${habit.title}")
            Result.success(habit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create habit")
            Result.failure(e)
        }
    }

    /**
     * Update existing habit.
     */
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        category: HabitCategory,
        color: String,
        icon: String,
        frequency: HabitFrequency,
        customSchedule: List<java.time.DayOfWeek>? = null,
        reminderTime: java.time.LocalTime? = null,
        reminderEnabled: Boolean = false
    ): Result<Unit> {
        return try {
            val existing = habitDao.getHabitByIdOnce(habitId)
                ?: return Result.failure(Exception("Habit not found"))

            val updated = existing.copy(
                title = title,
                description = description,
                category = category,
                color = color,
                icon = icon,
                frequency = frequency,
                customSchedule = customSchedule,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled,
                updatedAt = Instant.now(),
                syncState = SyncState.PENDING
            )

            habitDao.upsert(updated)
            queueSync("habit", habitId, "UPDATE", moshi.adapter(HabitEntity::class.java).toJson(updated))
            
            Timber.d("Updated habit: $habitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update habit")
            Result.failure(e)
        }
    }

    /**
     * Archive a habit (soft delete).
     */
    suspend fun archiveHabit(habitId: String): Result<Unit> {
        return try {
            habitDao.archiveHabit(habitId)
            queueSync("habit", habitId, "UPDATE", """{"isArchived": true}""")
            
            Timber.d("Archived habit: $habitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to archive habit")
            Result.failure(e)
        }
    }

    /**
     * Permanently delete a habit.
     */
    suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            habitDao.deleteById(habitId)
            habitStreakDao.deleteByHabitId(habitId)
            queueSync("habit", habitId, "DELETE", "{}")
            
            Timber.d("Deleted habit: $habitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete habit")
            Result.failure(e)
        }
    }

    // ======================
    // HABIT COMPLETION
    // ======================

    /**
     * Mark habit as completed for today (optimistic).
     */
    suspend fun completeHabit(
        habitId: String,
        mood: HabitMood? = null,
        note: String? = null
    ): Result<HabitLogEntity> {
        val userId = securePreferences.userId ?: return Result.failure(Exception("User not logged in"))

        return try {
            // Check if already completed today
            val today = LocalDate.now().toString()
            val existingLog = habitLogDao.getLogForDate(habitId, today)
            if (existingLog != null) {
                return Result.failure(Exception("Habit already completed today"))
            }

            val log = HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                completedAt = Instant.now(),
                mood = mood,
                note = note,
                syncState = SyncState.PENDING
            )

            habitLogDao.insert(log)
            
            // Update streak
            habitStreakDao.incrementStreak(habitId, userId, today)
            
            // Get current streak for milestone check
            val streak = habitStreakDao.getStreakOnce(habitId)
            
            // Queue for sync
            queueSync("habit_log", log.id, "CREATE", moshi.adapter(HabitLogEntity::class.java).toJson(log))
            
            // Glyph feedback
            if (streak != null && streak.currentStreak % 7 == 0 && streak.currentStreak > 0) {
                // Milestone celebration for weekly streaks
                glyphManager.playStreakMilestone(streak.currentStreak)
            } else {
                // Regular success animation
                glyphManager.playHabitSuccess()
            }
            
            Timber.d("Completed habit: $habitId with streak: ${streak?.currentStreak}")
            Result.success(log)
        } catch (e: Exception) {
            Timber.e(e, "Failed to complete habit")
            Result.failure(e)
        }
    }

    /**
     * Undo a completion.
     */
    suspend fun uncompleteHabit(habitId: String, date: String = LocalDate.now().toString()): Result<Unit> {
        return try {
            val log = habitLogDao.getLogForDate(habitId, date)
                ?: return Result.failure(Exception("No completion found for this date"))

            habitLogDao.deleteById(log.id)
            queueSync("habit_log", log.id, "DELETE", "{}")
            
            // Recalculate streak after deletion
            val userId = securePreferences.userId
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            recalculateStreak(habitId, userId)
            
            Timber.d("Uncompleted habit: $habitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to uncomplete habit")
            Result.failure(e)
        }
    }

    /**
     * Get completion logs for a habit.
     */
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>> {
        return habitLogDao.getLogsForHabit(habitId)
    }

    /**
     * Check if habit is completed today.
     */
    fun isCompletedToday(habitId: String): Flow<Boolean> {
        return habitLogDao.isCompletedToday(habitId)
    }

    // ======================
    // STREAK TRACKING
    // ======================

    /**
     * Get streak for a habit.
     */
    fun getStreak(habitId: String): Flow<HabitStreakEntity?> {
        return habitStreakDao.getStreak(habitId)
    }

    /**
     * Get longest current streak across all habits.
     */
    fun getLongestCurrentStreak(): Flow<HabitStreakEntity?> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(null)
        return habitStreakDao.getLongestCurrentStreak(userId)
    }

    // ======================
    // ANALYTICS
    // ======================

    /**
     * Get total completion count for user.
     */
    fun getTotalCompletions(): Flow<Int> {
        val userId = securePreferences.userId ?: return kotlinx.coroutines.flow.flowOf(0)
        return habitLogDao.getRecentLogs(userId, 0).map { it.size }
    }

    /**
     * Get completion rate for a habit.
     */
    suspend fun getCompletionRate(habitId: String): Float {
        val habit = habitDao.getHabitByIdOnce(habitId) ?: return 0f
        
        val daysSinceCreation = java.time.Duration.between(habit.createdAt, Instant.now())
            .toDays()
            .toInt()
            .coerceAtLeast(1)
        
        // Count actual completions
        val completionCount = habitLogDao.getLogsForHabit(habitId).first().size
        
        // Calculate expected completions based on frequency
        val expectedCompletions = when (habit.frequency) {
            HabitFrequency.DAILY -> daysSinceCreation
            HabitFrequency.WEEKLY -> (daysSinceCreation / 7.0).toInt()
            HabitFrequency.CUSTOM -> {
                val daysPerWeek = habit.customSchedule?.size ?: 7
                (daysSinceCreation * daysPerWeek / 7.0).toInt()
            }
        }.coerceAtLeast(1)
        
        return (completionCount.toFloat() / expectedCompletions).coerceIn(0f, 1f)
    }

    /**
     * Recalculate streak for a habit.
     */
    private suspend fun recalculateStreak(habitId: String, userId: String) {
        val logs = habitLogDao.getLogsForHabit(habitId).first()
        if (logs.isEmpty()) {
            // Reset streak if no logs
            habitStreakDao.incrementStreak(habitId, userId, LocalDate.now().toString())
            return
        }
        
        // Sort logs by date descending
        val sortedLogs = logs.sortedByDescending { it.completedAt }
        var currentStreak = 0
        var lastDate: LocalDate? = null
        
        // Calculate current streak from most recent
        for (log in sortedLogs) {
            val logDate = java.time.Instant.ofEpochMilli(log.completedAt.toEpochMilli())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            
            if (lastDate == null) {
                lastDate = logDate
                currentStreak = 1
            } else if (logDate.plusDays(1) == lastDate) {
                currentStreak++
                lastDate = logDate
            } else {
                break
            }
        }
        
        // Update streak entity
        val streak = habitStreakDao.getStreakOnce(habitId)
        if (streak != null) {
            habitStreakDao.incrementStreak(habitId, userId, LocalDate.now().toString())
        }
    }

    // ======================
    // SYNC OPERATIONS
    // ======================

    /**
     * Sync habits with backend.
     */
    suspend fun syncHabits(): Result<Unit> {
        return try {
            // Sync pending operations from queue
            val pendingOps = syncQueueDao.getPendingOperations()
            
            pendingOps.filter { it.entityType == "habit" || it.entityType == "habit_log" }.forEach { op ->
                try {
                    // Process each sync operation
                    when (op.operation) {
                        "CREATE" -> {
                            if (op.entityType == "habit") {
                                val entity = habitDao.getHabitByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitDto::class.java).toJson(dto)
                                    val payload = json.toRequestBody()
                                    apiService.create("habits", payload)
                                    habitDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            } else if (op.entityType == "habit_log") {
                                val entity = habitLogDao.getLogByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitLogDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitLogDto::class.java).toJson(dto)
                                    val payload = json.toRequestBody()
                                    apiService.create("habit-logs", payload)
                                    habitLogDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            }
                            Timber.d("Synced CREATE: ${op.entityType} ${op.entityId}")
                        }
                        "UPDATE" -> {
                            if (op.entityType == "habit") {
                                val entity = habitDao.getHabitByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitDto::class.java).toJson(dto)
                                    val payload = json.toRequestBody()
                                    apiService.update("habits", op.entityId, payload)
                                    habitDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            }
                            Timber.d("Synced UPDATE: ${op.entityType} ${op.entityId}")
                        }
                        "DELETE" -> {
                            if (op.entityType == "habit") {
                                apiService.delete("habits", op.entityId)
                            }
                            Timber.d("Synced DELETE: ${op.entityType} ${op.entityId}")
                        }
                    }
                    // Mark as synced
                    syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync operation: ${op.id}")
                    val newRetryCount = op.retryCount + 1
                    if (newRetryCount >= 3) {
                        syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                    } else {
                        syncQueueDao.updateRetry(op.id, newRetryCount, SyncStatus.PENDING)
                    }
                }
            }
            
            Timber.d("Synced habits successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync habits (offline?)")
            Result.failure(e)
        }
    }

    /**
     * Queue operation for background sync.
     */
    private suspend fun queueSync(entityType: String, entityId: String, operation: String, payload: String) {
        val syncOp = SyncOperationEntity(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }
}
