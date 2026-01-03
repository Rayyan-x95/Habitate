package com.ninety5.habitate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.dao.CommentDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.ChallengeDao
import com.ninety5.habitate.data.local.dao.HabitatDao
import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.LikeEntity
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.core.utils.DebugLogger
import com.ninety5.habitate.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import com.squareup.moshi.Moshi
import com.ninety5.habitate.data.local.entity.PostEntity
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.ninety5.habitate.data.remote.ProgressRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import com.ninety5.habitate.data.local.entity.FollowEntity

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val followDao: FollowDao,
    private val likeDao: LikeDao,
    private val commentDao: CommentDao,
    private val postDao: PostDao,
    private val taskDao: TaskDao,
    private val workoutDao: WorkoutDao,
    private val habitDao: HabitDao,
    private val challengeDao: ChallengeDao,
    private val habitatDao: HabitatDao,
    private val storyDao: StoryDao,
    private val chatDao: com.ninety5.habitate.data.local.dao.ChatDao,
    private val messageDao: com.ninety5.habitate.data.local.dao.MessageDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) : CoroutineWorker(context, params) {

    companion object {
        const val MAX_RETRIES = 5
        const val INITIAL_BACKOFF_MS = 1000L // 1 second
    }

    override suspend fun doWork(): Result {
        val pendingOperations = syncQueueDao.getPendingOperations()
        
        if (pendingOperations.isEmpty()) {
            return Result.success()
        }

        var hasError = false

        for (op in pendingOperations) {
            try {
                syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS)
                
                when (op.entityType) {
                    "message" -> {
                        if (op.operation == "CREATE") {
                            val dto = moshi.adapter(com.ninety5.habitate.data.remote.dto.MessageDto::class.java).fromJson(op.payload)
                            if (dto != null) {
                                apiService.sendMessage(dto.chatId, dto)
                                messageDao.updateStatus(op.entityId, com.ninety5.habitate.data.local.entity.MessageStatus.SENT)
                            }
                        }
                    }
                    "follow" -> {
                        if (op.operation == "CREATE") apiService.followUser(op.entityId)
                        else if (op.operation == "DELETE") apiService.unfollowUser(op.entityId)
                    }
                    "like" -> {
                        // entityId format: "userId_postId"
                        if (op.operation == "CREATE") {
                            apiService.likePost(op.entityId.split("_")[1])  // postId
                        } else if (op.operation == "DELETE") {
                            apiService.unlikePost(op.entityId.split("_")[1])  // postId
                        }
                    }
                    "comment" -> {
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = op.payload.toRequestBody(mediaType)
                        when (op.operation) {
                            "CREATE" -> apiService.createComment(body)
                            "DELETE" -> apiService.deleteComment(op.entityId)
                        }
                    }
                    "notification_read" -> {
                        apiService.markNotificationRead(op.entityId)
                    }
                    "notification_read_all" -> {
                        apiService.markAllNotificationsRead()
                    }
                    "story" -> {
                        if (op.operation == "CREATE") {
                            val adapter = moshi.adapter(StoryEntity::class.java)
                            var story = adapter.fromJson(op.payload) 
                                ?: throw Exception("Failed to parse story payload for operation ${op.id}")
                            
                            if (story.mediaUrl.startsWith("content://") || story.mediaUrl.startsWith("file://")) {
                                val newUrl = uploadMedia(Uri.parse(story.mediaUrl))
                                story = story.copy(mediaUrl = newUrl)
                                val newPayload = adapter.toJson(story)
                                val mediaType = "application/json; charset=utf-8".toMediaType()
                                apiService.create("stories", newPayload.toRequestBody(mediaType))
                            } else {
                                val mediaType = "application/json; charset=utf-8".toMediaType()
                                apiService.create("stories", op.payload.toRequestBody(mediaType))
                            }
                        } else if (op.operation == "DELETE") {
                            apiService.delete("stories", op.entityId)
                        }
                    }
                    "post" -> {
                        if (op.operation == "CREATE") {
                            // Parse payload
                            val adapter = moshi.adapter(PostEntity::class.java)
                            var post = adapter.fromJson(op.payload)
                                ?: throw Exception("Failed to parse post payload for operation ${op.id}")
                            
                            // Check for local URIs and upload
                            val newMediaUrls = post.mediaUrls.map { url ->
                                if (url.startsWith("content://") || url.startsWith("file://")) {
                                    uploadMedia(Uri.parse(url))
                                } else {
                                    url
                                }
                            }
                            
                            val mediaType = "application/json; charset=utf-8".toMediaType()
                            
                            // Update payload if changed
                            if (newMediaUrls != post.mediaUrls) {
                                post = post.copy(mediaUrls = newMediaUrls)
                                val newPayload = adapter.toJson(post)
                                apiService.create("feed", newPayload.toRequestBody(mediaType))
                            } else {
                                apiService.create("feed", op.payload.toRequestBody(mediaType))
                            }
                        } else if (op.operation == "DELETE") {
                            apiService.delete("feed", op.entityId)
                        }
                    }
                    else -> {
                        val path = when (op.entityType) {
                            "task" -> "tasks"
                            "workout" -> "workouts"
                            "habitat" -> "habitats"
                            "habit" -> "habits"
                            "challenge" -> "challenges"
                            else -> op.entityType + "s"
                        }
                        
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = op.payload.toRequestBody(mediaType)

                        when (op.operation) {
                            "CREATE" -> apiService.create(path, body)
                            "UPDATE" -> apiService.update(path, op.entityId, body)
                            "DELETE" -> apiService.delete(path, op.entityId)
                            else -> {
                                Timber.e("Unknown operation: ${op.operation}")
                                syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                                continue
                            }
                        }
                    }
                }
                
                syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED)
                
            } catch (e: Exception) {
                // Conflict Resolution Logging
                if (e is retrofit2.HttpException && e.code() == 409) {
                    Timber.w("Conflict detected for operation ${op.id} (Entity: ${op.entityType}, ID: ${op.entityId}). Server state differs from local. Treating as permanent failure (Server Wins).")
                    syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                    rollbackOptimisticUpdate(op)
                    continue
                }

                Timber.e(e, "Sync failed for operation ${op.id}, retry ${op.retryCount}")
                hasError = true
                
                if (op.retryCount >= MAX_RETRIES) {
                    syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                    Timber.e("Operation ${op.id} permanently failed after $MAX_RETRIES retries")
                    
                    // Compensating Transactions (Rollback Optimistic Updates)
                    rollbackOptimisticUpdate(op)
                } else {
                    // Exponential backoff: 1s, 2s, 4s, 8s, 16s
                    val backoffDelay = INITIAL_BACKOFF_MS * (2.0.pow(op.retryCount.toDouble())).toLong()
                    Timber.d("Scheduling retry ${op.retryCount + 1} with ${backoffDelay}ms backoff")
                    
                    syncQueueDao.updateRetry(op.id, op.retryCount + 1, SyncStatus.PENDING)
                }
            }
        }

        return if (hasError) Result.retry() else Result.success()
    }
    
    /**
     * Rollback optimistic updates when sync permanently fails.
     * Restores local DB to correct state.
     */
    private suspend fun rollbackOptimisticUpdate(op: com.ninety5.habitate.data.local.entity.SyncOperationEntity) {
        try {
            when (op.entityType) {
                "message" -> {
                    if (op.operation == "CREATE") {
                        messageDao.updateStatus(op.entityId, com.ninety5.habitate.data.local.entity.MessageStatus.FAILED)
                        Timber.d("Marked message as failed: ${op.entityId}")
                    }
                }
                "follow" -> {
                    val ids = op.entityId.split("_")
                    if (ids.size == 2) {
                        val followerId = ids[0]
                        val followingId = ids[1]
                        if (op.operation == "CREATE") {
                            followDao.delete(followerId, followingId)
                            Timber.d("Rolled back follow: $followerId -> $followingId")
                        } else if (op.operation == "DELETE") {
                            followDao.insert(FollowEntity(
                                followerId = followerId,
                                followingId = followingId,
                                createdAt = System.currentTimeMillis(),
                                syncState = SyncState.SYNCED
                            ))
                            Timber.d("Restored follow: $followerId -> $followingId")
                        }
                    }
                }
                "like" -> {
                    val ids = op.entityId.split("_")
                    if (ids.size == 2) {
                        val userId = ids[0]
                        val postId = ids[1]
                        if (op.operation == "CREATE") {
                            likeDao.delete(userId, postId)
                            Timber.d("Rolled back like: $userId on $postId")
                        } else if (op.operation == "DELETE") {
                            likeDao.insert(LikeEntity(
                                userId = userId,
                                postId = postId,
                                createdAt = System.currentTimeMillis(),
                                syncState = SyncState.SYNCED
                            ))
                            Timber.d("Restored like: $userId on $postId")
                        }
                    }
                }
                "comment" -> {
                    if (op.operation == "CREATE") {
                        commentDao.deleteById(op.entityId)
                        Timber.d("Rolled back comment: ${op.entityId}")
                    }
                    // DELETE rollback not needed (comment already gone)
                }
                "post" -> {
                    if (op.operation == "CREATE") {
                        postDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked post as failed: ${op.entityId}")
                    }
                }
                "task" -> {
                    if (op.operation == "CREATE") {
                        taskDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked task as failed: ${op.entityId}")
                    }
                }
                "workout" -> {
                    if (op.operation == "CREATE") {
                        workoutDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked workout as failed: ${op.entityId}")
                    }
                }
                "habit" -> {
                    if (op.operation == "CREATE") {
                        habitDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked habit as failed: ${op.entityId}")
                    }
                }
                "challenge" -> {
                    if (op.operation == "CREATE") {
                        challengeDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked challenge as failed: ${op.entityId}")
                    }
                }
                "habitat" -> {
                    if (op.operation == "CREATE") {
                        habitatDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked habitat as failed: ${op.entityId}")
                    }
                }
                "story" -> {
                    if (op.operation == "CREATE") {
                        storyDao.updateSyncState(op.entityId, SyncState.FAILED)
                        Timber.d("Marked story as failed: ${op.entityId}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rollback operation ${op.id}")
        }
    }

    private suspend fun uploadMedia(uri: Uri): String {
        val file = getFileFromUri(uri) ?: throw Exception("Failed to get file from URI: $uri")
        
        val requestBody = ProgressRequestBody(file, "image/*") { } // No progress callback needed for sync
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        
        return apiService.uploadMedia(part)
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = applicationContext.contentResolver.openInputStream(uri) ?: return null
            val file = File(applicationContext.cacheDir, "upload_${System.currentTimeMillis()}")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to create file from URI")
            null
        }
    }
}
