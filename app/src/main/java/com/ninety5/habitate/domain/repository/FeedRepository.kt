package com.ninety5.habitate.domain.repository

import androidx.paging.PagingData
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Post
import com.ninety5.habitate.domain.model.PostVisibility
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for feed/post operations.
 * All methods return domain [Post] models, never framework entities.
 */
interface FeedRepository {

    // ── Feeds ──────────────────────────────────────────────────────────
    /** Paginated public feed. */
    fun getFeedPagingData(): Flow<PagingData<Post>>

    /** Non-paged public feed (for widgets / previews). */
    fun getFeedPosts(): Flow<List<Post>>

    /** Posts by a single user (profile screen). */
    fun getPostsByUser(userId: String): Flow<List<Post>>

    /** Posts within a specific habitat. */
    fun getPostsByHabitat(habitatId: String): Flow<List<Post>>

    // ── Single post ────────────────────────────────────────────────────
    /** Observe a single post reactively. */
    fun observePost(postId: String): Flow<Post?>

    /** One-shot fetch with network fallback. */
    suspend fun getPost(postId: String): AppResult<Post>

    // ── Mutations ──────────────────────────────────────────────────────
    suspend fun createPost(
        text: String,
        mediaUrls: List<String>,
        visibility: PostVisibility,
        habitatId: String?
    ): AppResult<Post>

    suspend fun deletePost(postId: String): AppResult<Unit>

    /**
     * Toggle like / reaction on a post.
     * Implementation resolves the current userId internally.
     *
     * @param reactionType Optional reaction type (HEART, LIKE, etc.). null = toggle default.
     */
    suspend fun toggleLike(postId: String, reactionType: String? = null): AppResult<Unit>

    suspend fun archivePost(postId: String): AppResult<Unit>
    suspend fun unarchivePost(postId: String): AppResult<Unit>

    // ── Sync ───────────────────────────────────────────────────────────
    suspend fun refreshFeed(): AppResult<Unit>
}
