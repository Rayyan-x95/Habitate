package com.ninety5.habitate.data.local.dao

import androidx.room.*
import com.ninety5.habitate.data.local.entity.FollowEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Follow entity.
 * Handles all database operations for follow relationships.
 */
@Dao
interface FollowDao {
    
    /**
     * Check if a user is following another user.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId)")
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean>
    
    /**
     * Get a specific follow relationship.
     */
    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followingId = :followingId LIMIT 1")
    suspend fun getFollow(followerId: String, followingId: String): FollowEntity?
    
    /**
     * Get users that a user is following.
     */
    @Transaction
    @Query("""
        SELECT users.* 
        FROM users 
        INNER JOIN follows ON users.id = follows.followingId 
        WHERE follows.followerId = :userId 
        ORDER BY follows.createdAt DESC
    """)
    fun getFollowing(userId: String): Flow<List<UserEntity>>
    
    /**
     * Get users following a user.
     */
    @Transaction
    @Query("""
        SELECT users.* 
        FROM users 
        INNER JOIN follows ON users.id = follows.followerId 
        WHERE follows.followingId = :userId 
        ORDER BY follows.createdAt DESC
    """)
    fun getFollowers(userId: String): Flow<List<UserEntity>>
    
    /**
     * Get following count for a user.
     */
    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>
    
    /**
     * Get followers count for a user.
     */
    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId")
    fun getFollowersCount(userId: String): Flow<Int>
    
    /**
     * Insert a follow relationship.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(follow: FollowEntity)
    
    /**
     * Delete a follow relationship.
     */
    @Delete
    suspend fun delete(follow: FollowEntity)
    
    /**
     * Delete a follow relationship by composite key.
     */
    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun delete(followerId: String, followingId: String)
    
    /**
     * Get all follows pending sync.
     */
    @Query("SELECT * FROM follows WHERE syncState = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingSync(): List<FollowEntity>
    
    /**
     * Update sync state for a follow.
     */
    @Query("UPDATE follows SET syncState = :syncState WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun updateSyncState(followerId: String, followingId: String, syncState: SyncState)
    
    /**
     * Delete all follows (for testing/logout).
     */
    @Query("DELETE FROM follows")
    suspend fun deleteAll()
}

