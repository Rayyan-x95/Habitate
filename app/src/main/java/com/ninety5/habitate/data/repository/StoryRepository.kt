package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val apiService: ApiService,
    private val syncQueueDao: SyncQueueDao,
    private val authRepository: AuthRepository
) {

    fun getActiveStories(): Flow<List<StoryWithUser>> {
        val userId = authRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return storyDao.getActiveStories(userId, System.currentTimeMillis())
    }

    suspend fun createStory(mediaUri: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        val storyId = UUID.randomUUID().toString()
        val now = Instant.now()
        val expiresAt = now.plus(24, ChronoUnit.HOURS)
        val nowMillis = now.toEpochMilli()
        val expiresAtMillis = expiresAt.toEpochMilli()

        val story = StoryEntity(
            id = storyId,
            userId = userId,
            mediaUrl = mediaUri,
            createdAt = nowMillis,
            expiresAt = expiresAtMillis,
            syncState = SyncState.PENDING
        )

        storyDao.upsert(story)

        // Queue sync
        val payload = "{\"mediaUri\": \"$mediaUri\", \"expiresAt\": \"$expiresAt\"}"
        
        val op = SyncOperationEntity(
            entityType = "story",
            entityId = storyId,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = now,
            lastAttemptAt = null
        )
        syncQueueDao.insert(op)
    }

    suspend fun refreshStories() {
        try {
            // Clean up expired stories first
            // storyDao.deleteExpired() // Assuming this exists or we add it
            
            val storyDtos = apiService.getStories()
            val storyEntities = storyDtos.map { dto ->
                StoryEntity(
                    id = dto.id,
                    userId = dto.authorId,
                    mediaUrl = dto.mediaUri,
                    createdAt = dto.createdAt.toEpochMilli(),
                    expiresAt = dto.expiresAt.toEpochMilli(),
                    syncState = SyncState.SYNCED
                )
            }
            storyEntities.forEach { story ->
                storyDao.upsert(story)
            }
        } catch (e: Exception) {
            // Ignore network errors for now, rely on cache
        }
    }
}
