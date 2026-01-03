package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.LikeEntity
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import com.squareup.moshi.Moshi

import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toPostEntity
import com.ninety5.habitate.domain.mapper.toUserEntity

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import com.ninety5.habitate.data.local.HabitateDatabase

/**
 * Repository for managing feed data.
 * Handles fetching posts from local database and syncing with remote API.
 * 
 * UPDATED: Now uses LikeEntity for proper like tracking instead of denormalized flags.
 */
@Singleton
class FeedRepository @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val workoutDao: WorkoutDao,
    private val syncQueueDao: SyncQueueDao,
    private val likeDao: LikeDao,  // NEW: Proper like tracking
    private val moshi: Moshi,
    private val apiService: ApiService,
    private val database: HabitateDatabase // NEW: For RemoteMediator
) {

    /**
     * Get all public posts as a Flow (Paging).
     */
    @OptIn(ExperimentalPagingApi::class)
    fun getFeedPostsPaging(): Flow<PagingData<PostWithAuthor>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            remoteMediator = FeedRemoteMediator(apiService, database),
            pagingSourceFactory = { postDao.getPublicPostsPaging() }
        ).flow.map { pagingData ->
            pagingData.map { postWithDetails ->
                PostWithAuthor(
                    post = postWithDetails.post,
                    author = postWithDetails.author,
                    workout = postWithDetails.workout
                )
            }
        }
    }

    /**
     * Get all public posts as a Flow.
     */
    fun getFeedPosts(): Flow<List<PostWithAuthor>> {
        return postDao.getPublicPosts().map { posts ->
            posts.map { postWithDetails ->
                PostWithAuthor(
                    post = postWithDetails.post,
                    author = postWithDetails.author,
                    workout = postWithDetails.workout
                )
            }
        }
    }

    /**
     * Get posts by a specific user.
     */
    fun getPostsByUser(userId: String): Flow<List<PostWithAuthor>> {
        return postDao.getPostsByUser(userId).map { posts ->
            posts.map { postWithDetails ->
                PostWithAuthor(
                    post = postWithDetails.post,
                    author = postWithDetails.author,
                    workout = postWithDetails.workout
                )
            }
        }
    }

    /**
     * Get posts from a specific habitat.
     */
    fun getPostsByHabitat(habitatId: String): Flow<List<PostWithAuthor>> {
        return postDao.getPostsByHabitat(habitatId).map { posts ->
            posts.map { postWithDetails ->
                PostWithAuthor(
                    post = postWithDetails.post,
                    author = postWithDetails.author,
                    workout = postWithDetails.workout
                )
            }
        }
    }

    /**
     * Get a single post by ID.
     */
    fun getPostById(postId: String): Flow<PostEntity?> {
        return postDao.getPostById(postId)
    }

    /**
     * Search posts by content.
     */
    fun searchPosts(query: String): Flow<List<PostEntity>> {
        return postDao.searchPosts(query)
    }

    /**
     * Create a new post.
     */
    suspend fun createPost(post: PostEntity) {
        postDao.upsert(post.copy(syncState = SyncState.PENDING))
        
        val payload = moshi.adapter(PostEntity::class.java).toJson(post)
        val syncOp = SyncOperationEntity(
            entityType = "post",
            entityId = post.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Toggle like on a post. Properly creates/deletes LikeEntity.
     * 
     * @param userId ID of the user liking/unliking the post (typically current user)
     * @param postId ID of the post being liked/unliked
     * 
     * NEW IMPLEMENTATION:
     * - Creates/deletes LikeEntity in 'likes' table (source of truth)
     * - Updates denormalized Post.likesCount for performance
     * - Queues operation for background sync
     * - Optimistic update: changes reflect immediately in UI
     * 
     * BREAKING CHANGE from old implementation:
     * - Now requires userId parameter (no longer assumes from context)
     * - Uses LikeEntity instead of boolean flag on Post
     */
    suspend fun toggleLike(userId: String, postId: String, reactionType: String? = null) {
        val existingLike = likeDao.getLike(userId, postId)
        
        if (reactionType != null) {
            // Explicit reaction (Upsert)
            val likeEntity = LikeEntity(
                userId = userId,
                postId = postId,
                reactionType = reactionType,
                createdAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING
            )
            likeDao.insert(likeEntity)
            
            // Update denormalized count on Post
            val post = postDao.getPostByIdOneShot(postId)
            if (post != null) {
                val newCount = if (existingLike != null) post.likesCount else post.likesCount + 1
                postDao.updateLikeStatus(postId, isLiked = true, count = newCount, reactionType = reactionType)
            }
            
            // Queue for sync
            val payload = moshi.adapter(LikeEntity::class.java).toJson(likeEntity)
            val syncOp = SyncOperationEntity(
                entityType = "like",
                entityId = "${userId}_${postId}",
                operation = "CREATE",
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
            syncQueueDao.insert(syncOp)
        } else {
            if (existingLike == null) {
                // Create like (HEART)
                val likeEntity = LikeEntity(
                    userId = userId,
                    postId = postId,
                    reactionType = "HEART",
                    createdAt = System.currentTimeMillis(),
                    syncState = SyncState.PENDING
                )
                likeDao.insert(likeEntity)
                
                val post = postDao.getPostByIdOneShot(postId)
                if (post != null) {
                    postDao.updateLikeStatus(postId, isLiked = true, count = post.likesCount + 1, reactionType = "HEART")
                }
                
                val payload = moshi.adapter(LikeEntity::class.java).toJson(likeEntity)
                val syncOp = SyncOperationEntity(
                    entityType = "like",
                    entityId = "${userId}_${postId}",
                    operation = "CREATE",
                    payload = payload,
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
                syncQueueDao.insert(syncOp)
            } else {
                // Delete like
                likeDao.delete(userId, postId)
                
                val post = postDao.getPostByIdOneShot(postId)
                if (post != null) {
                    postDao.updateLikeStatus(postId, isLiked = false, count = maxOf(0, post.likesCount - 1), reactionType = null)
                }
                
                val syncOp = SyncOperationEntity(
                    entityType = "like",
                    entityId = "${userId}_${postId}",
                    operation = "DELETE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
                syncQueueDao.insert(syncOp)
            }
        }
    }
    
    /**
     * Check if current user has liked a post. Returns Flow for reactive UI.
     */
    fun isPostLiked(userId: String, postId: String): Flow<Boolean> {
        return likeDao.isLiked(userId, postId)
    }
    
    /**
     * Get all users who liked a specific post. Returns Flow for reactive updates.
     */
    fun getLikesForPost(postId: String): Flow<List<LikeEntity>> {
        return likeDao.getLikesForPost(postId)
    }
    
    /**
     * Get like count for a post. Returns Flow for real-time updates.
     */
    fun getLikesCount(postId: String): Flow<Int> {
        return likeDao.getLikesCount(postId)
    }
    
    /**
     * Get all posts liked by a user. Useful for "Liked Posts" screen.
     */
    fun getLikedPostsByUser(userId: String): Flow<List<LikeEntity>> {
        return likeDao.getLikesByUser(userId)
    }

    /**
     * Delete a post.
     */
    suspend fun deletePost(postId: String) {
        postDao.deleteById(postId)
        
        val syncOp = SyncOperationEntity(
            entityType = "post",
            entityId = postId,
            operation = "DELETE",
            payload = "",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Refresh feed from server.
     */
    suspend fun refreshFeed(): Result<Unit> {
        return try {
            val postDtos = apiService.getFeed()
            val posts = postDtos.map { it.toPostEntity() }
            val authors = postDtos.map { it.author.toUserEntity() }
            
            userDao.upsertAll(authors)
            postDao.upsertAll(posts)
            Result.success(Unit)
        } catch (e: Exception) {
            // If offline, we just return failure but local data is still available
            Result.failure(e)
        }
    }

    /**
     * Get posts pending sync.
     */
    suspend fun getPendingSyncPosts(): List<PostEntity> {
        return postDao.getPendingSyncPosts()
    }
}

/**
 * Data class combining a post with its author information.
 */
data class PostWithAuthor(
    val post: PostEntity,
    val author: UserEntity?,
    val workout: WorkoutEntity? = null
)
