package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.ChallengeDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.ChallengeProgressEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.model.Challenge
import com.ninety5.habitate.domain.model.ChallengeProgress
import com.ninety5.habitate.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) : ChallengeRepository {

    private val userId: String?
        get() = securePreferences.userId

    override fun observeActiveChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllChallenges().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChallenge(challengeId: String): AppResult<Challenge> {
        return try {
            val entity = challengeDao.getChallengeById(challengeId)
            val challenge = entity.firstOrNull()
                ?: return AppResult.Error(AppError.NotFound("Challenge not found"))
            AppResult.Success(challenge.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get challenge: $challengeId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun joinChallenge(challengeId: String): AppResult<Unit> {
        val currentUserId = userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val progress = ChallengeProgressEntity(
                id = UUID.randomUUID().toString(),
                challengeId = challengeId,
                userId = currentUserId,
                progress = 0.0,
                status = com.ninety5.habitate.data.local.entity.ChallengeStatus.JOINED,
                joinedAt = Instant.now(),
                updatedAt = Instant.now(),
                syncState = SyncState.PENDING
            )

            // Atomic: upsert progress + enqueue sync together
            challengeDao.upsertProgress(progress)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "challenge_join",
                    entityId = challengeId,
                    operation = "CREATE",
                    payload = """{"userId":"$currentUserId","challengeId":"$challengeId","progressId":"${progress.id}"}""",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to join challenge: $challengeId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun leaveChallenge(challengeId: String): AppResult<Unit> {
        val currentUserId = userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Update local progress to reflect withdrawal
            val existing = challengeDao.getProgress(challengeId, currentUserId).firstOrNull()
            if (existing != null) {
                challengeDao.upsertProgress(
                    existing.copy(
                        status = com.ninety5.habitate.data.local.entity.ChallengeStatus.FAILED,
                        syncState = SyncState.PENDING,
                        updatedAt = Instant.now()
                    )
                )
            }

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "challenge_leave",
                    entityId = challengeId,
                    operation = "DELETE",
                    payload = """{"userId":"$currentUserId","challengeId":"$challengeId"}""",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to leave challenge: $challengeId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun getLeaderboard(challengeId: String): AppResult<List<ChallengeProgress>> {
        return try {
            val response = apiService.getChallengeLeaderboard(challengeId)
            if (response.isSuccessful) {
                val entries = response.body() ?: emptyList()
                val progresses = entries.mapIndexed { index, dto ->
                    ChallengeProgress(
                        challengeId = challengeId,
                        userId = dto.userId,
                        currentValue = dto.score,
                        status = com.ninety5.habitate.domain.model.ChallengeStatus.ACTIVE,
                        rank = index + 1
                    )
                }
                AppResult.Success(progresses)
            } else {
                AppResult.Error(AppError.Server("Failed to fetch leaderboard: ${response.code()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get leaderboard: $challengeId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun updateProgress(challengeId: String, value: Double): AppResult<Unit> {
        val currentUserId = userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val existing = challengeDao.getProgress(challengeId, currentUserId).firstOrNull()
                ?: return AppResult.Error(AppError.NotFound("No progress found for this challenge"))

            challengeDao.upsertProgress(
                existing.copy(
                    progress = value,
                    updatedAt = Instant.now(),
                    syncState = SyncState.PENDING
                )
            )
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "challenge_progress",
                    entityId = challengeId,
                    operation = "UPDATE",
                    payload = """{"value": $value, "userId": "$currentUserId"}""",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update progress: $challengeId")
            AppResult.Error(AppError.from(e))
        }
    }
}
