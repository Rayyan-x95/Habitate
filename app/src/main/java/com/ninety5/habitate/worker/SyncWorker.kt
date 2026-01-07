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
import timber.log.Timber
import java.time.Instant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
     */
    private suspend fun rollbackOptimisticUpdate(op: com.ninety5.habitate.data.local.entity.SyncOperationEntity) {
        try {
            when (op.entityType) {
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
                        postDao.deleteById(op.entityId)
                        Timber.d("Rolled back post: ${op.entityId}")
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
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rollback operation ${op.id}")
        }
    }
}
