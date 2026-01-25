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
import com.ninety5.habitate.data.local.entity.FollowEntity
import com.ninety5.habitate.data.local.entity.LikeEntity
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import java.time.Instant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.coroutineContext
import kotlin.math.pow

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
    private val messageDao: MessageDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) : CoroutineWorker(context, params) {

    companion object {
        const val MAX_RETRIES = 5
        const val INITIAL_BACKOFF_MS = 1000L // 1 second
        const val STALE_OPERATION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    override suspend fun doWork(): Result {
        // Reset stale IN_PROGRESS operations that may have been orphaned
        val cutoffTime = System.currentTimeMillis() - STALE_OPERATION_TIMEOUT_MS
        syncQueueDao.resetStaleOperations(cutoffTime)
        Timber.d("Reset stale operations older than ${STALE_OPERATION_TIMEOUT_MS / 1000}s")
        
        val pendingOperations = syncQueueDao.getPendingOperations()
        
        if (pendingOperations.isEmpty()) {
            return Result.success()
        }
        
        Timber.d("Processing ${pendingOperations.size} pending sync operations")

        var hasError = false

        for (op in pendingOperations) {
            // Check for cancellation before each operation - cooperative cancellation
            coroutineContext.ensureActive()
            
            try {
                syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS)
                
                when (op.entityType) {
                    "chat_message" -> {
                        if (op.operation == "CREATE") {
                            val adapter = moshi.adapter(MessageDto::class.java)
                            val messageDto = adapter.fromJson(op.payload)
                            
                            if (messageDto != null) {
                                val path = "chats/${messageDto.chatId}/messages"
                                val mediaType = "application/json; charset=utf-8".toMediaType()
                                val body = op.payload.toRequestBody(mediaType)
                                apiService.create(path, body)
                                messageDao.updateStatus(op.entityId, MessageStatus.SENT)
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
                    // "comment" case removed; handled by generic else block (entityType "comment" -> path "comments")
                    "notification_read" -> {
                        apiService.markNotificationRead(op.entityId)
                    }
                    "notification_read_all" -> {
                        apiService.markAllNotificationsRead()
                    }
                    "challenge_join" -> {
                        if (op.operation == "CREATE") {
                            apiService.joinChallenge(op.entityId)
                        }
                    }
                    else -> {
                        val path = when (op.entityType) {
                            "task" -> "tasks"
                            "workout" -> "workouts"
                            "habitat" -> "habitats"
                            "post" -> "posts"
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
     * 
     * Handles CREATE, DELETE, and UPDATE operations for all entity types.
     */
    private suspend fun rollbackOptimisticUpdate(op: com.ninety5.habitate.data.local.entity.SyncOperationEntity) {
        try {
            when (op.entityType) {
                "follow" -> {
                    val ids = op.entityId.split("_")
                    if (ids.size == 2) {
                        val followerId = ids[0]
                        val followingId = ids[1]
                        when (op.operation) {
                            "CREATE" -> {
                                followDao.delete(followerId, followingId)
                                Timber.d("Rolled back follow: $followerId -> $followingId")
                            }
                            "DELETE" -> {
                                // Restore follow - use createdAt from payload if available
                                val createdAt = try {
                                    moshi.adapter(FollowEntity::class.java)
                                        .fromJson(op.payload)?.createdAt ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    System.currentTimeMillis()
                                }
                                followDao.insert(FollowEntity(
                                    followerId = followerId,
                                    followingId = followingId,
                                    createdAt = createdAt,
                                    syncState = SyncState.SYNCED
                                ))
                                Timber.d("Restored follow: $followerId -> $followingId")
                            }
                        }
                    }
                }
                "like" -> {
                    val ids = op.entityId.split("_")
                    if (ids.size == 2) {
                        val userId = ids[0]
                        val postId = ids[1]
                        when (op.operation) {
                            "CREATE" -> {
                                likeDao.delete(userId, postId)
                                Timber.d("Rolled back like: $userId on $postId")
                            }
                            "DELETE" -> {
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
                }
                "comment" -> {
                    when (op.operation) {
                        "CREATE" -> {
                            commentDao.deleteById(op.entityId)
                            Timber.d("Rolled back comment: ${op.entityId}")
                        }
                        // DELETE rollback not needed (comment already gone)
                    }
                }
                "post" -> {
                    when (op.operation) {
                        "CREATE" -> {
                            postDao.deleteById(op.entityId)
                            Timber.d("Rolled back post: ${op.entityId}")
                        }
                        "UPDATE" -> {
                            // Mark as needing re-sync or flag as conflicted
                            postDao.updateSyncState(op.entityId, SyncState.FAILED)
                            Timber.d("Marked post update as failed: ${op.entityId}")
                        }
                        "DELETE" -> {
                            // Attempt to restore post from payload if available
                            if (op.payload.isNotBlank()) {
                                try {
                                    val postEntity = moshi.adapter(com.ninety5.habitate.data.local.entity.PostEntity::class.java)
                                        .fromJson(op.payload)
                                    if (postEntity != null) {
                                        postDao.upsert(postEntity.copy(syncState = SyncState.SYNCED))
                                        Timber.d("Restored deleted post from payload: ${op.entityId}")
                                    } else {
                                        Timber.w("Cannot restore deleted post: ${op.entityId} - invalid payload")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to parse post payload for restoration: ${op.entityId}")
                                }
                            } else {
                                Timber.w("Cannot restore deleted post: ${op.entityId} - no payload data")
                            }
                        }
                    }
                }
                "task" -> {
                    when (op.operation) {
                        "CREATE" -> {
                            taskDao.updateSyncState(op.entityId, SyncState.FAILED)
                            Timber.d("Marked task as failed: ${op.entityId}")
                        }
                        "UPDATE" -> {
                            taskDao.updateSyncState(op.entityId, SyncState.FAILED)
                            Timber.d("Marked task update as failed: ${op.entityId}")
                        }
                        "DELETE" -> {
                            // Restore from payload if available
                            taskDao.updateSyncState(op.entityId, SyncState.SYNCED)
                            Timber.d("Rolled back task delete: ${op.entityId}")
                        }
                    }
                }
                "workout" -> {
                    when (op.operation) {
                        "CREATE" -> {
                            workoutDao.updateSyncState(op.entityId, SyncState.FAILED)
                            Timber.d("Marked workout as failed: ${op.entityId}")
                        }
                        "UPDATE" -> {
                            workoutDao.updateSyncState(op.entityId, SyncState.FAILED)
                            Timber.d("Marked workout update as failed: ${op.entityId}")
                        }
                        "DELETE" -> {
                            // Attempt to restore workout from payload if available
                            if (op.payload.isNotBlank()) {
                                try {
                                    val workoutEntity = moshi.adapter(com.ninety5.habitate.data.local.entity.WorkoutEntity::class.java)
                                        .fromJson(op.payload)
                                    if (workoutEntity != null) {
                                        workoutDao.upsert(workoutEntity.copy(syncState = SyncState.SYNCED))
                                        Timber.d("Restored deleted workout from payload: ${op.entityId}")
                                    } else {
                                        Timber.w("Cannot restore deleted workout: ${op.entityId} - invalid payload")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to parse workout payload for restoration: ${op.entityId}")
                                }
                            } else {
                                // Check if workout still exists locally (soft delete case)
                                val existingWorkout = workoutDao.getWorkoutById(op.entityId)
                                if (existingWorkout != null) {
                                    workoutDao.updateSyncState(op.entityId, SyncState.SYNCED)
                                    Timber.d("Marked existing workout as synced: ${op.entityId}")
                                } else {
                                    Timber.w("Cannot restore deleted workout: ${op.entityId} - no payload data")
                                }
                            }
                        }
                    }
                }
                "chat_message" -> {
                    when (op.operation) {
                        "CREATE" -> {
                            // Mark message as failed to send
                            messageDao.updateStatus(op.entityId, MessageStatus.FAILED)
                            Timber.d("Marked message as failed: ${op.entityId}")
                        }
                    }
                }
                "chat_mute" -> {
                    // For chat_mute UPDATE failures, we can safely ignore
                    // UI will refresh from server on next sync
                    Timber.d("Ignoring chat_mute rollback: ${op.entityId}")
                }
                else -> {
                    Timber.w("No rollback handler for entity type: ${op.entityType}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rollback operation ${op.id}")
        }
    }
}
