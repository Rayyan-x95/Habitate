package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.CommentDao
import com.ninety5.habitate.data.local.dao.CommentWithUser
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toCommentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.squareup.moshi.Moshi

/**
 * Repository for managing comment data.
 * Handles creating, fetching, and deleting comments with offline-first architecture.
 * 
 * FEATURES:
 * - Optimistic updates: Comments appear immediately in UI
 * - Background sync: SyncWorker handles server synchronization
 * - Reactive updates: Flow returns ensure UI stays current
 * - Cascading deletes: Foreign keys auto-delete comments when post/user deleted
 */
@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao,
    private val syncQueueDao: SyncQueueDao,
    private val moshi: Moshi,
    private val apiService: ApiService
) {

    /**
     * Get all comments for a post with author information.
     * Returns CommentWithUser objects via @Embedded + @Relation for efficient querying.
     * 
     * @param postId ID of the post
     * @return Flow emitting list of comments with user data
     */
    fun getCommentsForPost(postId: String): Flow<List<CommentWithUser>> {
        return commentDao.getCommentsWithUsersForPost(postId)
    }

    /**
     * Get comment count for a post. Returns Flow for real-time updates.
     * Useful for displaying "X comments" in post cards.
     */
    fun getCommentsCount(postId: String): Flow<Int> {
        return commentDao.getCommentsCount(postId)
    }

    /**
     * Get all comments by a specific user. Useful for user profile.
     */
    fun getCommentsByUser(userId: String): Flow<List<CommentEntity>> {
        return commentDao.getCommentsByUser(userId)
    }

    /**
     * Create a new comment on a post.
     * 
     * @param userId ID of the user creating the comment (typically current user)
     * @param postId ID of the post being commented on
     * @param text Comment text content
     * 
     * Optimistically saves to local DB, then queues for background sync.
     * If sync fails, SyncWorker will retry with exponential backoff.
     */
    suspend fun createComment(userId: String, postId: String, text: String): Result<String> {
        return try {
            val commentId = UUID.randomUUID().toString()
            val comment = CommentEntity(
                id = commentId,
                userId = userId,
                postId = postId,
                text = text,
                createdAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING
            )
            
            // Optimistic update - save locally
            commentDao.insert(comment)
            
            // Queue for background sync
            val payload = moshi.adapter(CommentEntity::class.java).toJson(comment)
            val syncOp = SyncOperationEntity(
                entityType = "comment",
                entityId = commentId,
                operation = "CREATE",
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
            syncQueueDao.insert(syncOp)
            
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a comment by ID.
     * 
     * @param commentId ID of the comment to delete
     * @param userId ID of the user requesting deletion (for authorization check)
     * 
     * IMPORTANT: Only allow deletion if userId matches comment.userId or user is admin.
     * Client-side check should be enforced in ViewModel, server validates on sync.
     */
    suspend fun deleteComment(commentId: String, userId: String): Result<Unit> {
        return try {
            // Client-side authorization check
            val comment = commentDao.getCommentByIdOneShot(commentId)
            if (comment == null) {
                return Result.failure(IllegalArgumentException("Comment not found"))
            }
            if (comment.userId != userId) {
                // Note: Admin role check will be handled by server
                return Result.failure(SecurityException("Unauthorized: Cannot delete another user's comment"))
            }
            
            // Optimistic update - delete locally
            commentDao.deleteById(commentId)
            
            // Queue for background sync
            val syncOp = SyncOperationEntity(
                entityType = "comment",
                entityId = commentId,
                operation = "DELETE",
                payload = "{}",
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
            syncQueueDao.insert(syncOp)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh comments for a post from server.
     * Typically called on pull-to-refresh or when viewing post details.
     */
    suspend fun refreshCommentsForPost(postId: String): Result<Unit> {
        return try {
            val commentDtos = apiService.getCommentsForPost(postId)
            val comments = commentDtos.map { it.toCommentEntity() }
            comments.forEach { comment ->
                commentDao.insert(comment)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get comments pending sync. Used by SyncWorker.
     * Internal method - not exposed to UI layer.
     */
    suspend fun getPendingSyncComments(): List<CommentEntity> {
        return commentDao.getPendingSync()
    }

    /**
     * Update comment sync state after server sync attempt.
     * Called by SyncWorker after successful/failed sync.
     */
    suspend fun updateCommentSyncState(commentId: String, syncState: SyncState) {
        commentDao.updateSyncState(commentId, syncState)
    }
}
