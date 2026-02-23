package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for user/profile operations.
 * All methods return domain [User] models, never framework entities.
 */
interface UserRepository {

    // ── Observation ─────────────────────────────────────────────────────

    /** Observe the current authenticated user's profile reactively. */
    fun observeCurrentUser(): Flow<User?>

    /** Observe any user's profile reactively. */
    fun observeUser(userId: String): Flow<User?>

    // ── Single-shot queries ─────────────────────────────────────────────

    /** Get the current user synchronously (from local cache). */
    suspend fun getCurrentUser(): User?

    /** Get a user by ID, refreshing from network if needed. */
    suspend fun getUser(userId: String): AppResult<User>

    /** Refresh user profile from the server. */
    suspend fun refreshUser(userId: String): AppResult<Unit>

    // ── Profile mutations ───────────────────────────────────────────────

    /** Update the current user's profile fields. */
    suspend fun updateProfile(displayName: String?, bio: String?, avatarUrl: String?): AppResult<Unit>

    /** Toggle stealth mode for the current user. */
    suspend fun toggleStealthMode(enabled: Boolean): AppResult<Unit>

    // ── Follow system ───────────────────────────────────────────────────

    /** Check if current user is following another user. */
    fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean>

    /** Follow a user (optimistic local + sync queue). */
    suspend fun followUser(targetUserId: String): AppResult<Unit>

    /** Unfollow a user (optimistic local + sync queue). */
    suspend fun unfollowUser(targetUserId: String): AppResult<Unit>

    /** Get followers of a user. */
    suspend fun getFollowers(userId: String): AppResult<List<User>>

    /** Get users that a user is following. */
    suspend fun getFollowing(userId: String): AppResult<List<User>>

    /** Observe follower count reactively. */
    fun observeFollowerCount(userId: String): Flow<Int>

    /** Observe following count reactively. */
    fun observeFollowingCount(userId: String): Flow<Int>

    // ── Search ──────────────────────────────────────────────────────────

    /** Search users by query (username or display name). */
    suspend fun searchUsers(query: String): AppResult<List<User>>
}
