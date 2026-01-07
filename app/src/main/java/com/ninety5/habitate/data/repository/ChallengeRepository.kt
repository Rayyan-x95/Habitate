package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.ChallengeDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.local.entity.ChallengeProgressEntity
import com.ninety5.habitate.data.local.entity.ChallengeStatus
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.ChallengeCreateRequest
import com.ninety5.habitate.data.remote.dto.LeaderboardEntryDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    fun getAllChallenges(): Flow<List<ChallengeEntity>> {
        return challengeDao.getAllChallenges()
    }

    fun getChallengeById(id: String): Flow<ChallengeEntity?> {
        return challengeDao.getChallengeById(id)
    }

    fun getChallengeProgress(challengeId: String, userId: String): Flow<ChallengeProgressEntity?> {
        return challengeDao.getProgress(challengeId, userId)
    }

    suspend fun joinChallenge(challengeId: String, userId: String) {
        val progress = ChallengeProgressEntity(
            id = UUID.randomUUID().toString(),
            challengeId = challengeId,
            userId = userId,
            progress = 0.0,
            status = ChallengeStatus.JOINED,
            joinedAt = Instant.now(),
            updatedAt = Instant.now(),
            syncState = SyncState.PENDING
        )
        challengeDao.upsertProgress(progress)
        
        val syncOp = SyncOperationEntity(
            entityType = "challenge_join",
            entityId = challengeId,
            operation = "CREATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun getLeaderboard(challengeId: String): Result<List<LeaderboardEntryDto>> {
        return try {
            val response = apiService.getChallengeLeaderboard(challengeId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch leaderboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChallenge(challenge: ChallengeEntity) {
        challengeDao.upsert(challenge.copy(syncState = SyncState.PENDING))

        // Create DTO for sync
        val request = ChallengeCreateRequest(
            title = challenge.title,
            description = challenge.description,
            metricType = challenge.metricType,
            targetValue = challenge.targetValue,
            startDate = challenge.startDate,
            endDate = challenge.endDate,
            habitatId = challenge.habitatId
        )
        
        val payload = moshi.adapter(ChallengeCreateRequest::class.java).toJson(request)
        val syncOp = SyncOperationEntity(
            entityType = "challenge",
            entityId = challenge.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }
}
