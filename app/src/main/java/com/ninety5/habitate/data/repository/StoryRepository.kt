package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.dao.StoryMuteDao
import com.ninety5.habitate.data.local.dao.StoryViewDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.StoryMuteEntity
import com.ninety5.habitate.data.local.entity.StoryViewEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val storyViewDao: StoryViewDao,
    private val storyMuteDao: StoryMuteDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val moshi: Moshi
) {

    suspend fun getActiveStories(): Flow<List<StoryWithUser>> {
        val userId = authRepository.getCurrentUserId() ?: return emptyFlow()
        return storyDao.getActiveStories(userId)
    }

    suspend fun refreshStories() {
        try {
            val remoteStories = apiService.getStories()
            val cutoffInstant = Instant.now().minusSeconds(24 * 60 * 60) // 24 hours ago
            val cutoffMs = cutoffInstant.toEpochMilli()
            
            remoteStories.forEach { dto ->
                // Only cache non-expired stories
                if (dto.createdAt.isAfter(cutoffInstant)) {
                    val entity = StoryEntity(
                        id = dto.id,
                        userId = dto.authorId,
                        mediaUrl = dto.mediaUri,
                        caption = dto.caption,
                        createdAt = dto.createdAt.toEpochMilli(),
                        expiresAt = dto.expiresAt.toEpochMilli(),
                        syncState = SyncState.SYNCED
                    )
                    storyDao.upsert(entity)
                }
            }
            
            // Clean up expired stories from local cache
            storyDao.deleteExpired(cutoffMs)
            
            Timber.d("StoryRepository: Refreshed ${remoteStories.size} stories from server")
        } catch (e: Exception) {
            Timber.e(e, "StoryRepository: Failed to refresh stories")
            throw e
        }
    }

    suspend fun createStory(story: StoryEntity) {
        storyDao.upsert(story.copy(syncState = SyncState.PENDING))
        
        val payload = moshi.adapter(StoryEntity::class.java).toJson(story)
        val syncOp = SyncOperationEntity(
            entityType = "story",
            entityId = story.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun recordView(storyId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        val view = StoryViewEntity(storyId = storyId, viewerId = userId)
        storyViewDao.insert(view)
        // Sync view to server (omitted)
    }

    suspend fun muteUser(mutedUserId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        val mute = StoryMuteEntity(userId = userId, mutedUserId = mutedUserId)
        storyMuteDao.insert(mute)
    }

    suspend fun unmuteUser(mutedUserId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        storyMuteDao.delete(userId, mutedUserId)
    }
}
