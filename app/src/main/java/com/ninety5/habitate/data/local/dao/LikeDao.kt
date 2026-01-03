package com.ninety5.habitate.data.local.dao

import androidx.room.*
import com.ninety5.habitate.data.local.entity.LikeEntity
import com.ninety5.habitate.data.local.entity.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Like entity.
 * Handles all database operations for likes.
 */
@Dao
interface LikeDao {
    
    /**
     * Get a specific like by user and post.
     */
    @Query("SELECT * FROM likes WHERE userId = :userId AND postId = :postId LIMIT 1")
    suspend fun getLike(userId: String, postId: String): LikeEntity?
    
    /**
     * Check if a user has liked a post.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE userId = :userId AND postId = :postId)")
    fun isLiked(userId: String, postId: String): Flow<Boolean>
    
    /**
     * Get all likes by a user.
     */
    @Query("SELECT * FROM likes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLikesByUser(userId: String): Flow<List<LikeEntity>>
    
    /**
     * Get all likes for a post.
     */
    @Query("SELECT * FROM likes WHERE postId = :postId ORDER BY createdAt DESC")
    fun getLikesForPost(postId: String): Flow<List<LikeEntity>>
    
    /**
     * Get like count for a post.
     */
    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    fun getLikesCount(postId: String): Flow<Int>
    
    /**
     * Insert or replace a like.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: LikeEntity)
    
    /**
     * Delete a like.
     */
    @Delete
    suspend fun delete(like: LikeEntity)
    
    /**
     * Delete a like by composite key.
     */
    @Query("DELETE FROM likes WHERE userId = :userId AND postId = :postId")
    suspend fun delete(userId: String, postId: String)
    
    /**
     * Get all likes pending sync.
     */
    @Query("SELECT * FROM likes WHERE syncState = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingSync(): List<LikeEntity>
    
    /**
     * Update sync state for a like.
     */
    @Query("UPDATE likes SET syncState = :syncState WHERE userId = :userId AND postId = :postId")
    suspend fun updateSyncState(userId: String, postId: String, syncState: SyncState)
    
    /**
     * Delete all likes (for testing/logout).
     */
    @Query("DELETE FROM likes")
    suspend fun deleteAll()
}
