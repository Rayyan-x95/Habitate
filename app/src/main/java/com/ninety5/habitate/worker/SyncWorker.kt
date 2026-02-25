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
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import java.time.Instant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
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

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val payloadMapAdapter by lazy {
        @Suppress("UNCHECKED_CAST")
        moshi.adapter<Map<String, Any?>>(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
        )
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
                executeOperation(op)
                
                syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED)
                
            } catch (e: Exception) {
                // Rethrow CancellationException for cooperative cancellation
                if (e is CancellationException) throw e

                val httpStatus = (e as? HttpException)?.code()

                // Conflict Resolution Logging
                if (httpStatus == 409) {
                    Timber.w("Conflict detected for operation ${op.id} (Entity: ${op.entityType}, ID: ${op.entityId}). Server state differs from local. Treating as permanent failure (Server Wins).")
                    syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                    rollbackOptimisticUpdate(op)
                    continue
                }

                // Validation errors are permanent and should not be retried.
                if (httpStatus == 422) {
                    Timber.w("Validation failed for operation ${op.id} (Entity: ${op.entityType}, ID: ${op.entityId}). Marking operation as failed.")
                    syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                    rollbackOptimisticUpdate(op)
                    continue
                }

                // Local contract errors (bad payload/entity type) are permanent.
                if (e is IllegalArgumentException) {
                    Timber.e(e, "Invalid sync operation ${op.id}: ${op.entityType}/${op.operation}")
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

    private suspend fun executeOperation(op: SyncOperationEntity) {
        when (op.entityType) {
            "message", "chat_message" -> syncMessageOperation(op)
            "message_reaction" -> syncMessageReactionOperation(op)
            "chat_mute" -> syncChatMuteOperation(op)

            "follow" -> syncFollowOperation(op)
            "like" -> syncLikeOperation(op)
            "notification_read" -> {
                if (op.operation == "UPDATE") {
                    apiService.markNotificationRead(op.entityId)
                } else {
                    throw IllegalArgumentException("Unsupported notification_read operation: ${op.operation}")
                }
            }
            "notification_read_all" -> {
                if (op.operation == "UPDATE") {
                    apiService.markAllNotificationsRead()
                } else {
                    throw IllegalArgumentException("Unsupported notification_read_all operation: ${op.operation}")
                }
            }

            "challenge_join" -> syncChallengeJoinOperation(op)
            "challenge_leave" -> syncChallengeLeaveOperation(op)
            "challenge_progress" -> syncChallengeProgressOperation(op)

            "task" -> syncCrudOperation(op, "tasks")
            "workout" -> syncCrudOperation(op, "workouts")
            "habitat" -> syncCrudOperation(op, "habitats")
            "post" -> syncCrudOperation(op, "posts")
            "comment" -> syncCrudOperation(op, "comments")
            "notification" -> syncCrudOperation(op, "notifications")
            "journal" -> syncCrudOperation(op, "journal")
            "story" -> syncCrudOperation(op, "stories")
            "habit" -> syncCrudOperation(op, "habits")
            "habit_log" -> syncCrudOperation(op, "habit-logs")
            "habit_streak" -> syncCrudOperation(op, "habit-streaks")
            "focus_session" -> syncCrudOperation(op, "focus-sessions")

            "user_profile" -> syncCrudOperation(op, "users")
            "habitat_join" -> syncHabitatJoinOperation(op)
            "habitat_leave" -> syncHabitatLeaveOperation(op)
            "habitat_member" -> syncHabitatMemberOperation(op)
            "story_save" -> syncStorySaveOperation(op)
            "story_mute" -> syncStoryMuteOperation(op)

            else -> throw IllegalArgumentException("Unsupported sync entity type: ${op.entityType}")
        }
    }

    private suspend fun syncCrudOperation(op: SyncOperationEntity, path: String) {
        when (op.operation) {
            "CREATE" -> apiService.create(path, requestBody(op.payload))
            "UPDATE" -> apiService.update(path, op.entityId, requestBody(op.payload))
            "DELETE" -> apiService.delete(path, op.entityId)
            else -> throw IllegalArgumentException("Unsupported operation ${op.operation} for ${op.entityType}")
        }
    }

    private suspend fun syncFollowOperation(op: SyncOperationEntity) {
        val targetUserId = op.entityId.substringAfter("_", "")
        require(targetUserId.isNotBlank() && op.entityId.contains("_")) {
            "Invalid follow entityId: ${op.entityId}"
        }

        when (op.operation) {
            "CREATE" -> {
                apiService.followUser(targetUserId)
            }
            "DELETE" -> {
                apiService.unfollowUser(targetUserId)
            }
            else -> throw IllegalArgumentException("Unsupported follow operation: ${op.operation}")
        }
    }

    private suspend fun syncLikeOperation(op: SyncOperationEntity) {
        val postId = op.entityId.substringAfter("_", "")
        if (postId.isBlank()) {
            throw IllegalArgumentException("Invalid like entityId: ${op.entityId}")
        }

        when (op.operation) {
            "CREATE" -> apiService.likePost(postId)
            "DELETE" -> apiService.unlikePost(postId)
            else -> throw IllegalArgumentException("Unsupported like operation: ${op.operation}")
        }
    }

    private suspend fun syncMessageOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "CREATE" -> {
                val messageDto = moshi.adapter(MessageDto::class.java).fromJson(op.payload)
                    ?: throw IllegalArgumentException("Invalid message payload for ${op.entityId}")
                val path = "chats/${messageDto.chatId}/messages"
                apiService.create(path, requestBody(op.payload))
                try {
                    messageDao.updateStatus(op.entityId, MessageStatus.SENT)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Failed to update message status to SENT for ${op.entityId}")
                }
            }
            "DELETE" -> {
                apiService.delete("messages", op.entityId)
            }
            else -> throw IllegalArgumentException("Unsupported message operation: ${op.operation}")
        }
    }

    private suspend fun syncMessageReactionOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "CREATE" -> apiService.create("message-reactions", requestBody(op.payload))
            "UPDATE" -> apiService.update("message-reactions", op.entityId, requestBody(op.payload))
            "DELETE" -> apiService.delete("message-reactions", op.entityId)
            else -> throw IllegalArgumentException("Unsupported message_reaction operation: ${op.operation}")
        }
    }

    private suspend fun syncChatMuteOperation(op: SyncOperationEntity) {
        val payload = parsePayload(op.payload)
        val chatId = payload.stringValue("chatId") ?: op.entityId

        when (op.operation) {
            "CREATE", "UPDATE" -> apiService.create("chats/$chatId/mute", requestBody(op.payload))
            "DELETE" -> apiService.delete("chats/$chatId", "mute")
            else -> throw IllegalArgumentException("Unsupported chat_mute operation: ${op.operation}")
        }
    }

    private suspend fun syncChallengeJoinOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "CREATE" -> apiService.joinChallenge(op.entityId)
            "DELETE" -> apiService.delete("challenges/${op.entityId}", "join")
            else -> throw IllegalArgumentException("Unsupported challenge_join operation: ${op.operation}")
        }
    }

    private suspend fun syncChallengeLeaveOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "DELETE" -> apiService.delete("challenges/${op.entityId}", "join")
            else -> throw IllegalArgumentException("Unsupported challenge_leave operation: ${op.operation}")
        }
    }

    private suspend fun syncChallengeProgressOperation(op: SyncOperationEntity) {
        if (op.operation != "UPDATE" && op.operation != "CREATE") {
            throw IllegalArgumentException("Unsupported challenge_progress operation: ${op.operation}")
        }
        val value = parsePayload(op.payload).doubleValue("value")
            ?: throw IllegalArgumentException("challenge_progress missing numeric value in payload")
        apiService.updateChallengeProgress(op.entityId, value)
    }

    private suspend fun syncHabitatJoinOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "CREATE" -> apiService.create("habitats/${op.entityId}/join", requestBody(op.payload))
            else -> throw IllegalArgumentException("Unsupported habitat_join operation: ${op.operation}")
        }
    }

    private suspend fun syncHabitatLeaveOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "DELETE" -> apiService.delete("habitats/${op.entityId}", "leave")
            else -> throw IllegalArgumentException("Unsupported habitat_leave operation: ${op.operation}")
        }
    }

    private suspend fun syncHabitatMemberOperation(op: SyncOperationEntity) {
        val payload = parsePayload(op.payload)
        val habitatId = payload.stringValue("habitatId") ?: op.entityId.substringBefore("_")
        val userId = payload.stringValue("userId") ?: op.entityId.substringAfter("_", "")

        if (habitatId.isBlank() || userId.isBlank()) {
            throw IllegalArgumentException("Invalid habitat_member payload/entityId: ${op.entityId}")
        }

        when (op.operation) {
            "UPDATE" -> apiService.update("habitats/$habitatId/members", userId, requestBody(op.payload))
            else -> throw IllegalArgumentException("Unsupported habitat_member operation: ${op.operation}")
        }
    }

    private suspend fun syncStorySaveOperation(op: SyncOperationEntity) {
        when (op.operation) {
            "CREATE" -> apiService.create("stories/${op.entityId}/save", requestBody(op.payload))
            "DELETE" -> apiService.delete("stories/${op.entityId}", "save")
            else -> throw IllegalArgumentException("Unsupported story_save operation: ${op.operation}")
        }
    }

    private suspend fun syncStoryMuteOperation(op: SyncOperationEntity) {
        val mutedUserId = parsePayload(op.payload).stringValue("mutedUserId") ?: op.entityId

        when (op.operation) {
            "CREATE" -> apiService.create("users/$mutedUserId/stories/mute", requestBody(op.payload))
            "DELETE" -> apiService.delete("users/$mutedUserId/stories", "mute")
            else -> throw IllegalArgumentException("Unsupported story_mute operation: ${op.operation}")
        }
    }

    private fun requestBody(payload: String) =
        payload.ifBlank { "{}" }.toRequestBody(jsonMediaType)

    private fun parsePayload(payload: String): Map<String, Any?> {
        if (payload.isBlank() || payload == "{}") return emptyMap()
        return try {
            payloadMapAdapter.fromJson(payload) ?: emptyMap()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun Map<String, Any?>.stringValue(key: String): String? {
        val value = this[key] ?: return null
        return when (value) {
            is String -> value
            else -> value.toString()
        }
    }

    private fun Map<String, Any?>.doubleValue(key: String): Double? {
        val value = this[key] ?: return null
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }
    
    /**
     * Rollback optimistic updates when sync permanently fails.
     * Restores local DB to correct state.
     * 
     * Handles CREATE, DELETE, and UPDATE operations for all entity types.
     */
    private suspend fun rollbackOptimisticUpdate(op: SyncOperationEntity) {
        try {
            when (op.entityType) {
                // Note: Some entity types (e.g., habit, habit_log, habit_streak, focus_session, journal, story, notification, habitat, user_profile, message_reaction, story_save, story_mute, habitat_join, habitat_leave, habitat_member, challenge_join, challenge_leave, challenge_progress)
                // intentionally rely on server-refresh instead of local rollback to avoid complex compensation logic.
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
                                } catch (e: CancellationException) {
                                    throw e
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
                                } catch (e: CancellationException) {
                                    throw e
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
                                } catch (e: CancellationException) {
                                    throw e
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
                "message", "chat_message" -> {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to rollback operation ${op.id}")
        }
    }
}
