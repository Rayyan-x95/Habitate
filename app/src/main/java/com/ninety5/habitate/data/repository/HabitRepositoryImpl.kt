package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.HabitLogDao
import com.ninety5.habitate.data.local.dao.HabitStreakDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.*
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.HabitDto
import com.ninety5.habitate.data.remote.dto.HabitLogDto
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.mapper.toEntityCategory
import com.ninety5.habitate.domain.mapper.toEntityFrequency
import com.ninety5.habitate.domain.mapper.toEntityMood
import com.ninety5.habitate.domain.model.Habit
import com.ninety5.habitate.domain.model.HabitLog
import com.ninety5.habitate.domain.model.HabitMood
import com.ninety5.habitate.domain.model.HabitStreak
import com.ninety5.habitate.domain.model.HabitWithDetails
import com.ninety5.habitate.domain.repository.HabitRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [HabitRepository].
 *
 * Handles:
 * - CRUD operations for habits
 * - Completion logging with mood tracking
 * - Streak calculation and maintenance
 * - Offline-first data syncing
 * - Glyph visual feedback (Nothing phones)
 */
@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitStreakDao: HabitStreakDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi,
    private val glyphManager: com.ninety5.habitate.core.glyph.HabitateGlyphManager
) : HabitRepository {

    // ══════════════════════════════════════════════════════════════════════
    // OBSERVE
    // ══════════════════════════════════════════════════════════════════════

    override fun observeAllHabits(): Flow<List<Habit>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitDao.getAllHabits(userId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeActiveHabits(): Flow<List<Habit>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitDao.getActiveHabits(userId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeActiveHabitsWithStreaks(): Flow<List<HabitWithDetails>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitDao.getActiveHabitsWithStreaks(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeHabitWithDetails(habitId: String): Flow<HabitWithDetails?> {
        return combine(
            habitDao.getHabitWithLogs(habitId),
            habitStreakDao.getStreak(habitId)
        ) { habitWithLogs, streak ->
            habitWithLogs?.toDomain(streak)
        }
    }

    override fun observeStreak(habitId: String): Flow<HabitStreak?> {
        return habitStreakDao.getStreak(habitId).map { it?.toDomain() }
    }

    override fun isCompletedToday(habitId: String): Flow<Boolean> {
        return habitLogDao.isCompletedToday(habitId)
    }

    // ══════════════════════════════════════════════════════════════════════
    // ONE-SHOT
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getHabit(habitId: String): AppResult<Habit> {
        return try {
            val userId = securePreferences.userId
                ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))
            val entity = habitDao.getHabitByIdOnce(habitId)
                ?: return AppResult.Error(AppError.NotFound("Habit not found"))
            if (entity.userId != userId) {
                return AppResult.Error(AppError.Forbidden("Not authorized to view this habit"))
            }
            AppResult.Success(entity.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MUTATIONS
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun createHabit(habit: Habit): AppResult<Habit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val entity = habit.copy(
                id = if (habit.id.isBlank()) UUID.randomUUID().toString() else habit.id,
                userId = userId,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ).toEntity(SyncState.PENDING)

            habitDao.upsert(entity)

            // Initialize streak
            habitStreakDao.upsert(HabitStreakEntity(habitId = entity.id, userId = userId))

            // Queue for sync
            queueSync("habit", entity.id, "CREATE", moshi.adapter(HabitEntity::class.java).toJson(entity))

            Timber.d("Created habit: ${entity.title}")
            AppResult.Success(entity.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to create habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun updateHabit(habit: Habit): AppResult<Unit> {
        return try {
            val userId = securePreferences.userId
                ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))
            val existing = habitDao.getHabitByIdOnce(habit.id)
                ?: return AppResult.Error(AppError.NotFound("Habit not found"))
            if (existing.userId != userId) {
                return AppResult.Error(AppError.Forbidden("Not authorized to update this habit"))
            }

            val updated = habit.copy(updatedAt = Instant.now())
                .toEntity(SyncState.PENDING)
                .copy(reminderEnabled = existing.reminderEnabled) // preserve field not in domain

            habitDao.upsert(updated)
            queueSync("habit", habit.id, "UPDATE", moshi.adapter(HabitEntity::class.java).toJson(updated))

            Timber.d("Updated habit: ${habit.id}")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun deleteHabit(habitId: String): AppResult<Unit> {
        return try {
            val userId = securePreferences.userId
                ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))
            val existing = habitDao.getHabitByIdOnce(habitId)
                ?: return AppResult.Error(AppError.NotFound("Habit not found"))
            if (existing.userId != userId) {
                return AppResult.Error(AppError.Forbidden("Not authorized to delete this habit"))
            }
            habitDao.deleteById(habitId)
            habitLogDao.deleteByHabitId(habitId)
            habitStreakDao.deleteByHabitId(habitId)
            queueSync("habit", habitId, "DELETE", "{}")
            queueSync("habit_log", habitId, "DELETE", """{"habitId":"$habitId"}""")
            queueSync("habit_streak", habitId, "DELETE", """{"habitId":"$habitId"}""")
            Timber.d("Deleted habit: $habitId")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun archiveHabit(habitId: String): AppResult<Unit> {
        return try {
            habitDao.archiveHabit(habitId, System.currentTimeMillis())
            queueSync("habit", habitId, "UPDATE", """{"isArchived": true}""")
            Timber.d("Archived habit: $habitId")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to archive habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // COMPLETION TRACKING
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun logCompletion(habitId: String, mood: HabitMood?, note: String?): AppResult<HabitLog> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Check if already completed today
            val today = LocalDate.now().toString()
            val existingLog = habitLogDao.getLogForDate(habitId, today)
            if (existingLog != null) {
                return AppResult.Error(AppError.Unknown("Habit already completed today"))
            }

            val log = HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                completedAt = Instant.now(),
                mood = mood?.toEntityMood(),
                note = note,
                syncState = SyncState.PENDING
            )

            habitLogDao.insert(log)
            habitStreakDao.incrementStreak(habitId, userId, today)

            val streak = habitStreakDao.getStreakOnce(habitId)
            queueSync("habit_log", log.id, "CREATE", moshi.adapter(HabitLogEntity::class.java).toJson(log))

            // Glyph feedback
            if (streak != null && streak.currentStreak % 7 == 0 && streak.currentStreak > 0) {
                glyphManager.playStreakMilestone(streak.currentStreak)
            } else {
                glyphManager.playHabitSuccess()
            }

            Timber.d("Completed habit: $habitId, streak: ${streak?.currentStreak}")
            AppResult.Success(log.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to complete habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun undoCompletion(habitId: String, date: String): AppResult<Unit> {
        return try {
            val log = habitLogDao.getLogForDate(habitId, date)
                ?: return AppResult.Error(AppError.NotFound("No completion found for this date"))

            habitLogDao.deleteById(log.id)
            queueSync("habit_log", log.id, "DELETE", "{}")

            val userId = securePreferences.userId
                ?: return AppResult.Error(AppError.Unauthorized("User not authenticated"))
            recalculateStreak(habitId, userId)

            Timber.d("Uncompleted habit: $habitId")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to uncomplete habit")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun getCompletionHistory(habitId: String, limit: Int): AppResult<List<HabitLog>> {
        return try {
            val logs = habitLogDao.getLogsForHabitLimited(habitId, limit)
                .map { it.toDomain() }
            AppResult.Success(logs)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get completion history")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SYNC
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun syncHabits(): AppResult<Unit> {
        return try {
            val pendingOps = syncQueueDao.getPendingOperations()

            pendingOps.filter { it.entityType == "habit" || it.entityType == "habit_log" }.forEach { op ->
                try {
                    when (op.operation) {
                        "CREATE" -> {
                            if (op.entityType == "habit") {
                                val entity = habitDao.getHabitByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitDto::class.java).toJson(dto)
                                    apiService.create("habits", json.toRequestBody())
                                    habitDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            } else if (op.entityType == "habit_log") {
                                val entity = habitLogDao.getLogByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitLogDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitLogDto::class.java).toJson(dto)
                                    apiService.create("habit-logs", json.toRequestBody())
                                    habitLogDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            }
                        }
                        "UPDATE" -> {
                            if (op.entityType == "habit") {
                                val entity = habitDao.getHabitByIdOnce(op.entityId)
                                if (entity != null) {
                                    val dto = HabitDto.fromEntity(entity)
                                    val json = moshi.adapter(HabitDto::class.java).toJson(dto)
                                    apiService.update("habits", op.entityId, json.toRequestBody())
                                    habitDao.upsert(entity.copy(syncState = SyncState.SYNCED))
                                }
                            }
                        }
                        "DELETE" -> {
                            if (op.entityType == "habit") {
                                apiService.delete("habits", op.entityId)
                            } else if (op.entityType == "habit_log") {
                                apiService.delete("habit-logs", op.entityId)
                            }
                        }
                    }
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
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync habits (offline?)")
            AppResult.Error(AppError.NoConnection(e.message ?: "No internet connection", e))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERNAL
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun recalculateStreak(habitId: String, userId: String) {
        val logs = habitLogDao.getLogsForHabit(habitId).first()
        val streak = habitStreakDao.getStreakOnce(habitId) ?: return

        if (logs.isEmpty()) {
            habitStreakDao.upsert(streak.copy(
                currentStreak = 0,
                lastCompletedDate = null,
                totalCompletions = 0
            ))
            return
        }

        val sortedLogs = logs.sortedByDescending { it.completedAt }
        var currentStreak = 0
        var lastDate: java.time.LocalDate? = null

        for (log in sortedLogs) {
            val logDate = Instant.ofEpochMilli(log.completedAt.toEpochMilli())
                .atZone(java.time.ZoneOffset.UTC)
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

        habitStreakDao.upsert(streak.copy(
            currentStreak = currentStreak,
            longestStreak = maxOf(streak.longestStreak, currentStreak),
            totalCompletions = logs.size,
            lastCompletedDate = sortedLogs.firstOrNull()?.let {
                Instant.ofEpochMilli(it.completedAt.toEpochMilli())
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                    .toString()
            }
        ))
    }

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
