package com.ninety5.habitate.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.StoryViewEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for story creation payload serialization
 */
data class StoryPayload(
    val mediaUri: String,
    val expiresAt: String,
    val caption: String? = null
)

@Singleton
class StoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val apiService: ApiService,
    private val syncQueueDao: SyncQueueDao,
    private val authRepository: AuthRepository,
    private val moshi: Moshi
) {

    fun getActiveStories(): Flow<List<StoryWithUser>> {
        val userId = authRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return storyDao.getActiveStories(userId, System.currentTimeMillis())
    }

    suspend fun createStory(mediaUri: String, caption: String? = null) {
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
            caption = caption,
            createdAt = nowMillis,
            expiresAt = expiresAtMillis,
            syncState = SyncState.PENDING
        )

        storyDao.upsert(story)

        // Queue sync using proper JSON serialization
        val storyPayload = StoryPayload(
            mediaUri = mediaUri,
            expiresAt = expiresAt.toString(),
            caption = caption
        )
        val payload = moshi.adapter(StoryPayload::class.java).toJson(storyPayload)
        
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

    suspend fun markStoryAsSeen(storyId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        val viewEntity = StoryViewEntity(
            storyId = storyId,
            viewerId = userId,
            viewedAt = System.currentTimeMillis()
        )
        try {
            storyDao.insertStoryView(viewEntity)
        } catch (e: SQLiteConstraintException) {
            // Expected: duplicate view (user already saw this story)
            Timber.d("Story $storyId already viewed by user $userId")
        } catch (e: Exception) {
            // Log unexpected errors
            Timber.e(e, "Failed to mark story as seen: $storyId")
            throw e
        }
    }

    suspend fun refreshStories() {
        try {
            // Clean up expired stories first
            storyDao.deleteExpiredStories(System.currentTimeMillis())
            
            val storyDtos = apiService.getStories()
            val storyEntities = storyDtos.map { dto ->
                StoryEntity(
                    id = dto.id,
                    userId = dto.authorId,
                    mediaUrl = dto.mediaUri,
                    caption = dto.caption,
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
