package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.FollowEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toUserEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

import com.squareup.moshi.Moshi

/**
 * Repository for User-related operations.
 * Manages user profiles and follow relationships with offline-first architecture.
 * 
 * IMPORTANT: Follow operations now use composite keys (followerId, followingId)
 * to support bidirectional relationships. Always provide both IDs.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val followDao: FollowDao,  // Updated from FollowingDao
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    /**
     * Get user profile by ID. Returns Flow for reactive updates.
     */
    fun getUser(userId: String): Flow<UserEntity?> {
        return userDao.getUser(userId)
    }

    /**
     * Check if followerId is following followingId.
     * IMPORTANT: Requires both IDs due to composite key.
     * 
     * Example: isFollowing("currentUserId", "profileUserId")
     */
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean> {
        return followDao.isFollowing(followerId, followingId)
    }
    
    /**
     * Get all users that the specified user is following.
     */
    fun getFollowing(userId: String): Flow<List<UserEntity>> {
        return followDao.getFollowing(userId)
    }
    
    /**
     * Get all users following the specified user.
     */
    fun getFollowers(userId: String): Flow<List<UserEntity>> {
        return followDao.getFollowers(userId)
    }
    
    /**
     * Get following count for user (people they follow).
     */
    fun getFollowingCount(userId: String): Flow<Int> {
        return followDao.getFollowingCount(userId)
    }
    
    /**
     * Get follower count for user (people following them).
     */
    fun getFollowersCount(userId: String): Flow<Int> {
        return followDao.getFollowersCount(userId)
    }

    /**
     * Refresh user profile from server.
     */
    suspend fun refreshUser(userId: String): Result<Unit> {
        return try {
            val userDto = apiService.getUser(userId)
            userDao.upsert(userDto.toUserEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Follow a user. Creates FollowEntity with composite key.
     * 
     * @param followerId ID of the user performing the follow action (typically current user)
     * @param followingId ID of the user being followed
     * 
     * Optimistically updates local DB, then queues for sync with server.
     * If sync fails, SyncWorker will retry with exponential backoff.
     */
    suspend fun followUser(followerId: String, followingId: String) {
        // Optimistic update - create follow relationship locally
        val followEntity = FollowEntity(
            followerId = followerId,
            followingId = followingId,
            createdAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING
        )
        followDao.insert(followEntity)

        // Queue for background sync
        val payload = moshi.adapter(FollowEntity::class.java).toJson(followEntity)
        val syncOp = SyncOperationEntity(
            entityType = "follow",
            entityId = "${followerId}_${followingId}",  // Composite ID for deduplication
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Unfollow a user. Deletes FollowEntity by composite key.
     * 
     * @param followerId ID of the user performing the unfollow action
     * @param followingId ID of the user being unfollowed
     */
    suspend fun unfollowUser(followerId: String, followingId: String) {
        // Optimistic update - delete follow relationship locally
        followDao.delete(followerId, followingId)

        // Queue for background sync
        val syncOp = SyncOperationEntity(
            entityType = "follow",
            entityId = "${followerId}_${followingId}",
            operation = "DELETE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }
    
    suspend fun updateProfile(user: UserEntity) {
        userDao.upsert(user)
        
        val payload = moshi.adapter(UserEntity::class.java).toJson(user)
        val syncOp = SyncOperationEntity(
            entityType = "user_profile",
            entityId = user.id,
            operation = "UPDATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun updateStealthMode(userId: String, isStealthMode: Boolean) {
        userDao.updateStealthMode(userId, isStealthMode)
        
        val user = userDao.getUserByIdOnce(userId)
        if (user != null) {
            val updatedUser = user.copy(isStealthMode = isStealthMode)
            val payload = moshi.adapter(UserEntity::class.java).toJson(updatedUser)
            val syncOp = SyncOperationEntity(
                entityType = "user_profile",
                entityId = userId,
                operation = "UPDATE",
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
            syncQueueDao.insert(syncOp)
        }
    }
}
