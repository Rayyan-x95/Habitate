package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.FocusDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.FocusSessionEntity
import com.ninety5.habitate.data.local.entity.FocusSessionStatus
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.FocusSession
import com.ninety5.habitate.domain.model.FocusStatus
import com.ninety5.habitate.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val focusDao: FocusDao,
    private val syncQueueDao: SyncQueueDao,
    private val securePreferences: SecurePreferences
) : FocusRepository {

    private val userId: String
        get() = securePreferences.userId
            ?: error("userId must not be null — user is not authenticated")

    override fun observeActiveSessions(): Flow<FocusSession?> {
        return focusDao.getActiveSessionFlow().map { it?.toDomain() }
    }

    override fun observeSessionHistory(): Flow<List<FocusSession>> {
        return focusDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun startSession(
        durationSeconds: Long,
        soundTrack: String?
    ): AppResult<FocusSession> {
        return try {
            // Abort any existing in-progress session
            val existing = focusDao.getCurrentSession()
            if (existing != null) {
                val abortedSession = existing.copy(
                    status = FocusSessionStatus.ABORTED,
                    endTime = Instant.now(),
                    syncState = SyncState.PENDING,
                    updatedAt = Instant.now()
                )
                focusDao.upsert(abortedSession)

                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "focus_session",
                        entityId = existing.id,
                        operation = "UPDATE",
                        payload = """{"status": "ABORTED"}""",
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }

            val now = Instant.now()
            val id = UUID.randomUUID().toString()
            val entity = FocusSessionEntity(
                id = id,
                userId = userId,
                startTime = now,
                endTime = null,
                durationSeconds = durationSeconds,
                status = FocusSessionStatus.IN_PROGRESS,
                soundTrack = soundTrack,
                rating = null,
                syncState = SyncState.PENDING,
                updatedAt = now
            )
            focusDao.upsert(entity)

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "focus_session",
                    entityId = id,
                    operation = "CREATE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = now,
                    lastAttemptAt = null
                )
            )
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to start focus session")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun pauseSession(sessionId: String): AppResult<Unit> {
        return updateSessionStatus(sessionId, FocusSessionStatus.PAUSED)
    }

    override suspend fun resumeSession(sessionId: String): AppResult<Unit> {
        return updateSessionStatus(sessionId, FocusSessionStatus.IN_PROGRESS)
    }

    override suspend fun completeSession(
        sessionId: String,
        rating: Int?
    ): AppResult<FocusSession> {
        return try {
            val existing = focusDao.getCurrentSession()
                ?: return AppResult.Error(AppError.NotFound("Session not found"))
            if (existing.id != sessionId) {
                return AppResult.Error(AppError.NotFound("Session not found"))
            }

            val now = Instant.now()
            val completed = existing.copy(
                status = FocusSessionStatus.COMPLETED,
                endTime = now,
                rating = rating,
                syncState = SyncState.PENDING,
                updatedAt = now
            )
            focusDao.upsert(completed)

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "focus_session",
                    entityId = sessionId,
                    operation = "UPDATE",
                    payload = """{"status": "COMPLETED", "rating": $rating}""",
                    status = SyncStatus.PENDING,
                    createdAt = now,
                    lastAttemptAt = null
                )
            )
            AppResult.Success(completed.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to complete focus session: $sessionId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun cancelSession(sessionId: String): AppResult<Unit> {
        return updateSessionStatus(sessionId, FocusSessionStatus.ABORTED)
    }

    override suspend fun getTotalFocusMinutesToday(): AppResult<Long> {
        return try {
            val startOfDay = LocalDate.now()
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val sessions = focusDao.getSessionsSince(startOfDay)
            val list = sessions.firstOrNull() ?: emptyList()
            val totalMinutes = list
                .filter { it.status == FocusSessionStatus.COMPLETED }
                .sumOf { it.durationSeconds } / 60
            AppResult.Success(totalMinutes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get total focus minutes today")
            AppResult.Error(AppError.from(e))
        }
    }

    // ── Internal ────────────────────────────────────────────────────────

    override suspend fun saveCompletedSession(
        durationSeconds: Long,
        soundTrack: String?
    ): AppResult<FocusSession> {
        return try {
            val now = Instant.now()
            val id = UUID.randomUUID().toString()
            val entity = FocusSessionEntity(
                id = id,
                userId = userId,
                startTime = now.minusSeconds(durationSeconds),
                endTime = now,
                durationSeconds = durationSeconds,
                status = FocusSessionStatus.COMPLETED,
                soundTrack = soundTrack,
                rating = null,
                syncState = SyncState.PENDING,
                updatedAt = now
            )
            focusDao.upsert(entity)

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "focus_session",
                    entityId = id,
                    operation = "CREATE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = now,
                    lastAttemptAt = null
                )
            )
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to save completed focus session")
            AppResult.Error(AppError.from(e))
        }
    }

    private suspend fun updateSessionStatus(
        sessionId: String,
        status: FocusSessionStatus
    ): AppResult<Unit> {
        return try {
            val existing = focusDao.getCurrentSession()
                ?: return AppResult.Error(AppError.NotFound("Session not found"))
            if (existing.id != sessionId) {
                return AppResult.Error(AppError.NotFound("Session not found"))
            }

            val now = Instant.now()
            val endTime = if (status == FocusSessionStatus.COMPLETED ||
                status == FocusSessionStatus.ABORTED
            ) now else existing.endTime // Preserve existing endTime for pause/resume

            focusDao.upsert(
                existing.copy(
                    status = status,
                    endTime = endTime,
                    syncState = SyncState.PENDING,
                    updatedAt = now
                )
            )

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "focus_session",
                    entityId = sessionId,
                    operation = "UPDATE",
                    payload = """{"status": "$status"}""",
                    status = SyncStatus.PENDING,
                    createdAt = now,
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update session status: $sessionId")
            AppResult.Error(AppError.from(e))
        }
    }
}
