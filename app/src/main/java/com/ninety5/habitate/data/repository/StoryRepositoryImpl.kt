package com.ninety5.habitate.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
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
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.mapper.toEntityVisibility
import com.ninety5.habitate.domain.model.PostVisibility
import com.ninety5.habitate.domain.model.Story
import com.ninety5.habitate.domain.repository.StoryRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Concrete implementation of [StoryRepository].
 *
 * Handles:
 * - Story creation with 24h expiry
 * - Active story feed with mute filtering
 * - Story views tracking (idempotent)
 * - Story save/unsave
 * - User story muting
 * - Offline-first with sync queue
 */
@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val storyViewDao: StoryViewDao,
    private val storyMuteDao: StoryMuteDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi
) : StoryRepository {

    // ══════════════════════════════════════════════════════════════════════
    // DOMAIN INTERFACE METHODS
    // ══════════════════════════════════════════════════════════════════════

    override fun observeActiveStories(): Flow<List<Story>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return storyDao.getActiveStories(userId, System.currentTimeMillis())
            .map { storiesWithUser ->
                storiesWithUser.map { it.toDomain() }
            }
    }

    override suspend fun createStory(
        mediaUrl: String,
        caption: String?,
        visibility: String
    ): AppResult<Story> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val storyId = UUID.randomUUID().toString()
            val now = Instant.now()
            val expiresAt = now.plus(24, ChronoUnit.HOURS)

            val entityVisibility = try {
                Visibility.valueOf(visibility.uppercase())
            } catch (_: Exception) {
                Visibility.PUBLIC
            }

            val entity = StoryEntity(
                id = storyId,
                userId = userId,
                mediaUrl = mediaUrl,
                caption = caption,
                visibility = entityVisibility,
                createdAt = now.toEpochMilli(),
                expiresAt = expiresAt.toEpochMilli(),
                syncState = SyncState.PENDING
            )

            storyDao.upsert(entity)

            val payloadMap = buildMap {
                put("mediaUrl", mediaUrl)
                put("visibility", visibility)
                put("expiresAt", expiresAt.toString())
                if (caption != null) put("caption", caption)
            }
            @Suppress("UNCHECKED_CAST")
            val payloadJson = moshi.adapter<Map<String, String>>(
                Map::class.java
            ).toJson(payloadMap as Map<String, String>)
            queueSync("story", storyId, "CREATE", payloadJson)

            Timber.d("Created story: $storyId")
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to create story")
            AppResult.Error(AppError.Database(e.message ?: "Failed to create story"))
        }
    }

    override suspend fun deleteStory(storyId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Verify ownership before deleting
            val story = storyDao.getStoryById(storyId)
            if (story != null && story.userId != userId) {
                return AppResult.Error(AppError.Unauthorized("Cannot delete another user's story"))
            }

            storyDao.deleteById(storyId)
            queueSync("story", storyId, "DELETE", "{}")

            Timber.d("Deleted story: $storyId")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete story: $storyId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to delete story"))
        }
    }

    override suspend fun markAsViewed(storyId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val view = StoryViewEntity(
                storyId = storyId,
                viewerId = userId,
                viewedAt = System.currentTimeMillis()
            )
            storyViewDao.insert(view) // IGNORE on conflict (idempotent)

            Timber.d("Marked story $storyId as viewed")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark story as viewed: $storyId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to mark story as viewed"))
        }
    }

    override suspend fun saveStory(storyId: String): AppResult<Unit> {
        return try {
            storyDao.updateSaved(storyId, true)
            queueSync("story_save", storyId, "CREATE", """{"storyId":"$storyId"}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to save story: $storyId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to save story"))
        }
    }

    override suspend fun muteUserStories(userId: String): AppResult<Unit> {
        val currentUserId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val mute = StoryMuteEntity(
                userId = currentUserId,
                mutedUserId = userId
            )
            storyMuteDao.insert(mute)
            queueSync("story_mute", userId, "CREATE", """{"mutedUserId":"$userId"}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to mute user stories: $userId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to mute user stories"))
        }
    }

    override suspend fun unmuteUserStories(userId: String): AppResult<Unit> {
        val currentUserId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            storyMuteDao.delete(currentUserId, userId)
            queueSync("story_mute", userId, "DELETE", """{"mutedUserId":"$userId"}""")

            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to unmute user stories: $userId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to unmute user stories"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEGACY VIEWMODEL-FACING METHODS
    // These support existing ViewModels that haven't migrated to domain interface.
    // ══════════════════════════════════════════════════════════════════════

    fun getActiveStories(): Flow<List<StoryWithUser>> {
        val userId = securePreferences.userId
            ?: return flowOf(emptyList())
        return storyDao.getActiveStories(userId, System.currentTimeMillis())
    }

    suspend fun createStory(mediaUri: String, caption: String? = null) {
        createStory(mediaUri, caption, "PUBLIC")
    }

    suspend fun markStoryAsSeen(storyId: String) {
        markAsViewed(storyId)
    }

    override suspend fun refreshStories(): AppResult<Unit> {
        return try {
            storyDao.deleteExpiredStories(System.currentTimeMillis())
            val storyDtos = apiService.getStories()
            storyDtos.forEach { dto ->
                storyDao.upsert(dto.toEntity())
            }
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh stories (offline)")
            AppResult.Error(AppError.Network(e.message ?: "Failed to refresh stories"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun queueSync(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String
    ) {
        syncQueueDao.insert(
            SyncOperationEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
        )
    }
}
