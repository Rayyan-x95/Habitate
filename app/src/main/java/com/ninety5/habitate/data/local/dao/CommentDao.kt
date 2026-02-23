package com.ninety5.habitate.data.local.dao

import androidx.room.*
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Comment entity.
 * Handles all database operations for comments.
 */
@Dao
interface CommentDao {
    
    /**
     * Get a comment by ID.
     */
    @Query("SELECT * FROM comments WHERE id = :id LIMIT 1")
    suspend fun getCommentById(id: String): CommentEntity?
    
    /**
     * Get a comment by ID (one-shot, non-Flow version for transactions).
     */
    @Query("SELECT * FROM comments WHERE id = :id LIMIT 1")
    suspend fun getCommentByIdOneShot(id: String): CommentEntity?
    
    /**
     * Get all comments for a post with user information.
     */
    @Transaction
    @Query("""
        SELECT comments.*
        FROM comments 
        INNER JOIN users ON comments.userId = users.id 
        WHERE comments.postId = :postId 
        ORDER BY comments.createdAt DESC
    """)
    fun getCommentsWithUsersForPost(postId: String): Flow<List<CommentWithUser>>
    
    /**
     * Get comments for a post (without user).
     */
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt DESC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>
    
    /**
     * Get comment count for a post.
     */
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    fun getCommentsCount(postId: String): Flow<Int>
    
    /**
     * Get all comments by a user.
     */
    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCommentsByUser(userId: String): Flow<List<CommentEntity>>
    
    /**
     * Insert a comment.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    /**
     * Batch upsert comments. Replaces existing entries on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(comments: List<CommentEntity>)
    
    /**
     * Delete a comment.
     */
    @Delete
    suspend fun delete(comment: CommentEntity)
    
    /**
     * Delete a comment by ID.
     */
    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Get all comments pending sync.
     */
    @Query("SELECT * FROM comments WHERE syncState = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingSync(): List<CommentEntity>
    
    /**
     * Update sync state for a comment.
     */
    @Query("UPDATE comments SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: SyncState)
    
    /**
     * Delete all comments (for testing/logout).
     */
    @Query("DELETE FROM comments")
    suspend fun deleteAll()
}

