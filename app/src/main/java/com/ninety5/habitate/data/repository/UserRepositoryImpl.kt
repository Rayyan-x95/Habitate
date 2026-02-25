package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.entity.FollowEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toUserEntity
import com.ninety5.habitate.domain.model.User
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.UserRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Concrete implementation of [UserRepository].
 *
 * Follows offline-first architecture: local writes are immediate and
 * server synchronization is queued via [SyncQueueDao].
 *
 * Uses [Provider<AuthRepository>] to break circular dependency with Hilt.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val followDao: FollowDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi,
    private val authRepositoryProvider: Provider<AuthRepository>
) : UserRepository {

    private val authRepository: AuthRepository get() = authRepositoryProvider.get()

    // ── Observation ─────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> {
        return authRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(null)
            else userDao.getUser(userId).map { it?.toDomain() }
        }
    }

    override fun observeUser(userId: String): Flow<User?> {
        return userDao.getUser(userId).map { it?.toDomain() }
    }

    // ── Single-shot queries ─────────────────────────────────────────────

    override suspend fun getCurrentUser(): User? {
        val userId = authRepository.getCurrentUserId() ?: return null
        return userDao.getUserByIdOnce(userId)?.toDomain()
    }

    override suspend fun getUser(userId: String): AppResult<User> {
        return try {
            // Try local cache first
            val cached = userDao.getUserByIdOnce(userId)
            if (cached != null) {
                return AppResult.Success(cached.toDomain())
            }
            // Fallback to network
            val dto = apiService.getUser(userId)
            userDao.upsert(dto.toUserEntity())
            val entity = userDao.getUserByIdOnce(userId)
            if (entity != null) {
                AppResult.Success(entity.toDomain())
            } else {
                AppResult.Error(AppError.NotFound("User not found"))
            }
        } catch (e: SocketTimeoutException) {
            Timber.e(e, "Timeout fetching user $userId")
            AppResult.Error(AppError.Timeout("Request timed out"))
        } catch (e: IOException) {
            Timber.e(e, "Network error fetching user $userId")
            AppResult.Error(AppError.NoConnection("No internet connection"))
        } catch (e: retrofit2.HttpException) {
            Timber.e(e, "HTTP error fetching user $userId")
            when (e.code()) {
                401 -> AppResult.Error(AppError.Unauthorized("Session expired"))
                404 -> AppResult.Error(AppError.NotFound("User not found"))
                else -> AppResult.Error(AppError.Unknown(e.message ?: "Unknown error", e))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user $userId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun refreshUser(userId: String): AppResult<Unit> {
        return try {
            val dto = apiService.getUser(userId)
            userDao.upsert(dto.toUserEntity())
            AppResult.Success(Unit)
        } catch (e: SocketTimeoutException) {
            AppResult.Error(AppError.Timeout("Request timed out"))
        } catch (e: IOException) {
            AppResult.Error(AppError.NoConnection("No internet connection"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh user $userId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    // ── Profile mutations ───────────────────────────────────────────────

    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        avatarUrl: String?
    ): AppResult<Unit> {
        return try {
            val userId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            val existing = userDao.getUserByIdOnce(userId)
                ?: return AppResult.Error(AppError.NotFound("User not found locally"))

            val updated = existing.copy(
                displayName = displayName ?: existing.displayName,
                bio = bio ?: existing.bio,
                avatarUrl = avatarUrl ?: existing.avatarUrl
            )

            // Optimistic local update
            userDao.upsert(updated)

            // Queue for server sync
            val payload = moshi.adapter(com.ninety5.habitate.data.local.entity.UserEntity::class.java).toJson(updated)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "user_profile",
                    entityId = userId,
                    operation = "UPDATE",
                    payload = payload,
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update profile")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun toggleStealthMode(enabled: Boolean): AppResult<Unit> {
        return try {
            val userId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            userDao.updateStealthMode(userId, enabled)

            val user = userDao.getUserByIdOnce(userId)
            if (user != null) {
                val payload = moshi.adapter(com.ninety5.habitate.data.local.entity.UserEntity::class.java).toJson(user)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "user_profile",
                        entityId = userId,
                        operation = "UPDATE",
                        payload = payload,
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle stealth mode")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    // ── Follow system ───────────────────────────────────────────────────

    override fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean> {
        return followDao.isFollowing(currentUserId, targetUserId)
    }

    override suspend fun followUser(targetUserId: String): AppResult<Unit> {
        return try {
            val currentUserId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            val entity = FollowEntity(
                followerId = currentUserId,
                followingId = targetUserId,
                createdAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING
            )
            followDao.insert(entity)

            val payload = moshi.adapter(FollowEntity::class.java).toJson(entity)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "follow",
                    entityId = "${currentUserId}_${targetUserId}",
                    operation = "CREATE",
                    payload = payload,
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to follow user $targetUserId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun unfollowUser(targetUserId: String): AppResult<Unit> {
        return try {
            val currentUserId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            followDao.delete(currentUserId, targetUserId)

            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "follow",
                    entityId = "${currentUserId}_${targetUserId}",
                    operation = "DELETE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to unfollow user $targetUserId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun getFollowers(userId: String): AppResult<List<User>> {
        return try {
            // Attempt network refresh in the background
            try {
                val remoteFollowers = apiService.getUserFollowers(userId)
                val entities = remoteFollowers.map { it.toUserEntity() }
                userDao.upsertAll(entities)
            } catch (e: IOException) {
                Timber.w(e, "Network unavailable, using cached followers for $userId")
            } catch (e: SocketTimeoutException) {
                Timber.w(e, "Network timeout, using cached followers for $userId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to refresh followers from network for $userId")
            }
            
            // Re-read after potential network update
            val updatedFollowers = followDao.getFollowersOnce(userId)
            AppResult.Success(updatedFollowers.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to get followers for $userId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun getFollowing(userId: String): AppResult<List<User>> {
        return try {
            // Attempt network refresh in the background
            try {
                val remoteFollowing = apiService.getUserFollowing(userId)
                val entities = remoteFollowing.map { it.toUserEntity() }
                userDao.upsertAll(entities)
            } catch (e: IOException) {
                Timber.w(e, "Network unavailable, using cached following for $userId")
            } catch (e: SocketTimeoutException) {
                Timber.w(e, "Network timeout, using cached following for $userId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to refresh following from network for $userId")
            }
            
            // Re-read after potential network update
            val updatedFollowing = followDao.getFollowingOnce(userId)
            AppResult.Success(updatedFollowing.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to get following for $userId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override fun observeFollowerCount(userId: String): Flow<Int> {
        return followDao.getFollowersCount(userId)
    }

    override fun observeFollowingCount(userId: String): Flow<Int> {
        return followDao.getFollowingCount(userId)
    }

    // ── Search ──────────────────────────────────────────────────────────

    override suspend fun searchUsers(query: String): AppResult<List<User>> {
        return try {
            // Local search only — server endpoint not yet available
            val results = userDao.searchUsers("%$query%")
            AppResult.Success(results.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to search users")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }
}
