package com.ninety5.habitate.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.HabitateDatabase
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.entity.LikeEntity
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntityVisibility
import com.ninety5.habitate.domain.mapper.toPostEntity
import com.ninety5.habitate.domain.mapper.toUserEntity
import com.ninety5.habitate.domain.model.Post
import com.ninety5.habitate.domain.model.PostVisibility
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.FeedRepository
import com.squareup.moshi.Moshi
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Concrete implementation of [FeedRepository].
 *
 * Follows offline-first architecture: local writes are immediate and
 * server synchronization is queued via [SyncQueueDao].
 *
 * Uses [Provider<AuthRepository>] to break circular dependency with Hilt.
 */
@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val likeDao: LikeDao,
    private val moshi: Moshi,
    private val apiService: ApiService,
    private val database: HabitateDatabase,
    private val authRepositoryProvider: Provider<AuthRepository>
) : FeedRepository {

    private val authRepository: AuthRepository get() = authRepositoryProvider.get()

    // ── Feeds ──────────────────────────────────────────────────────────

    @OptIn(ExperimentalPagingApi::class)
    override fun getFeedPagingData(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            remoteMediator = FeedRemoteMediator(apiService, database),
            pagingSourceFactory = { postDao.getPublicPostsPaging() }
        ).flow.map { pagingData ->
            pagingData.map { postWithDetails ->
                postWithDetails.post.toDomain(author = postWithDetails.author)
            }
        }
    }

    override fun getFeedPosts(): Flow<List<Post>> {
        return postDao.getPublicPosts().map { posts ->
            posts.map { it.post.toDomain(author = it.author) }
        }
    }

    override fun getPostsByUser(userId: String): Flow<List<Post>> {
        return postDao.getPostsByUser(userId).map { posts ->
            posts.map { it.post.toDomain(author = it.author) }
        }
    }

    override fun getPostsByHabitat(habitatId: String): Flow<List<Post>> {
        return postDao.getPostsByHabitat(habitatId).map { posts ->
            posts.map { it.post.toDomain(author = it.author) }
        }
    }

    // ── Single post ────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePost(postId: String): Flow<Post?> {
        return postDao.getPostById(postId).mapLatest { entity ->
            if (entity != null) {
                val author = userDao.getUserByIdOnce(entity.authorId)
                entity.toDomain(author = author)
            } else null
        }
    }

    override suspend fun getPost(postId: String): AppResult<Post> {
        return try {
            val cached = postDao.getPostByIdOneShot(postId)
            if (cached != null) {
                val author = userDao.getUserByIdOnce(cached.authorId)
                return AppResult.Success(cached.toDomain(author = author))
            }
            AppResult.Error(AppError.NotFound("Post not found"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to get post $postId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    // ── Mutations ──────────────────────────────────────────────────────

    override suspend fun createPost(
        text: String,
        mediaUrls: List<String>,
        visibility: PostVisibility,
        habitatId: String?
    ): AppResult<Post> {
        return try {
            val userId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            val postId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val entity = PostEntity(
                id = postId,
                authorId = userId,
                contentText = text,
                mediaUrls = mediaUrls,
                visibility = visibility.toEntityVisibility(),
                habitatId = habitatId,
                workoutId = null,
                syncState = SyncState.PENDING,
                createdAt = now,
                updatedAt = now
            )

            database.withTransaction {
                postDao.upsert(entity)

                val payload = moshi.adapter(PostEntity::class.java).toJson(entity)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "post",
                        entityId = postId,
                        operation = "CREATE",
                        payload = payload,
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }

            val author = userDao.getUserByIdOnce(userId)
            AppResult.Success(entity.toDomain(author = author))
        } catch (e: Exception) {
            Timber.e(e, "Failed to create post")
            AppResult.Error(AppError.Database(e.message ?: "Database error"))
        }
    }

    override suspend fun deletePost(postId: String): AppResult<Unit> {
        return try {
            database.withTransaction {
                postDao.deleteById(postId)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "post",
                        entityId = postId,
                        operation = "DELETE",
                        payload = "",
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete post $postId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun toggleLike(postId: String, reactionType: String?): AppResult<Unit> {
        return try {
            val userId = authRepository.getCurrentUserId()
                ?: return AppResult.Error(AppError.Unauthorized("Not logged in"))

            val currentTime = System.currentTimeMillis()

            if (reactionType != null) {
                val (isNew, _) = likeDao.upsertReactionAtomic(
                    userId = userId,
                    postId = postId,
                    reactionType = reactionType,
                    currentTimeMillis = currentTime,
                    syncState = SyncState.PENDING
                )
                if (isNew) {
                    postDao.incrementLikeCount(postId, reactionType)
                }
                // Else: reaction type changed but like already counted — no count change needed
                val likeEntity = LikeEntity(
                    userId = userId, postId = postId, reactionType = reactionType,
                    createdAt = currentTime, syncState = SyncState.PENDING
                )
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "like", entityId = "${userId}_${postId}", operation = "CREATE",
                        payload = moshi.adapter(LikeEntity::class.java).toJson(likeEntity),
                        status = SyncStatus.PENDING, createdAt = Instant.now(), lastAttemptAt = null
                    )
                )
            } else {
                val wasCreated = likeDao.toggleLikeAtomic(
                    userId = userId, postId = postId, reactionType = "HEART",
                    currentTimeMillis = currentTime, syncState = SyncState.PENDING
                )
                if (wasCreated) {
                    postDao.incrementLikeCount(postId, "HEART")
                } else {
                    postDao.decrementLikeCount(postId)
                }
                val syncOp = if (wasCreated) {
                    val likeEntity = LikeEntity(
                        userId = userId, postId = postId, reactionType = "HEART",
                        createdAt = currentTime, syncState = SyncState.PENDING
                    )
                    SyncOperationEntity(
                        entityType = "like", entityId = "${userId}_${postId}", operation = "CREATE",
                        payload = moshi.adapter(LikeEntity::class.java).toJson(likeEntity),
                        status = SyncStatus.PENDING, createdAt = Instant.now(), lastAttemptAt = null
                    )
                } else {
                    SyncOperationEntity(
                        entityType = "like", entityId = "${userId}_${postId}", operation = "DELETE",
                        payload = "{}", status = SyncStatus.PENDING,
                        createdAt = Instant.now(), lastAttemptAt = null
                    )
                }
                syncQueueDao.insert(syncOp)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle like on post $postId")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun archivePost(postId: String): AppResult<Unit> {
        return try {
            val post = postDao.getPostByIdOneShot(postId)
                ?: return AppResult.Error(AppError.NotFound("Post not found"))
            postDao.upsert(post.copy(isArchived = true))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    override suspend fun unarchivePost(postId: String): AppResult<Unit> {
        return try {
            val post = postDao.getPostByIdOneShot(postId)
                ?: return AppResult.Error(AppError.NotFound("Post not found"))
            postDao.upsert(post.copy(isArchived = false))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    // ── Sync ───────────────────────────────────────────────────────────

    override suspend fun refreshFeed(): AppResult<Unit> {
        return try {
            val postDtos = apiService.getFeed()
            val posts = postDtos.map { it.toPostEntity() }
            val authors = postDtos.map { it.author.toUserEntity() }
            userDao.upsertAll(authors)
            postDao.upsertAll(posts)
            AppResult.Success(Unit)
        } catch (e: SocketTimeoutException) {
            AppResult.Error(AppError.Timeout("Request timed out"))
        } catch (e: IOException) {
            AppResult.Error(AppError.NoConnection("No internet connection"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh feed")
            AppResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    // ── Internal (not part of domain contract) ─────────────────────────

    /** Used by [SyncWorker] — not exposed through domain interface. */
    suspend fun getPendingSyncPosts(): List<PostEntity> {
        return postDao.getPendingSyncPosts()
    }
}
