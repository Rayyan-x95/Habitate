package com.ninety5.habitate.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Post
import com.ninety5.habitate.domain.model.PostVisibility
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.FeedRepository
import com.ninety5.habitate.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Feed screen.
 * Manages feed state including posts, stories, and user interactions.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    val pagingDataFlow: Flow<PagingData<PostUiModel>> = feedRepository.getFeedPagingData()
        .map { pagingData ->
            pagingData.map { it.toUiModel() }
        }
        .cachedIn(viewModelScope)

    init {
        loadStories()
    }

    fun loadFeed(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                try {
                    awaitAll(
                        async { feedRepository.refreshFeed() },
                        async { storyRepository.refreshStories() }
                    )
                } catch (e: Exception) {
                    timber.log.Timber.e(e, "Failed to refresh feed")
                    _uiState.update { it.copy(error = e.message ?: "Failed to refresh feed") }
                } finally {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.observeActiveStories().collect { stories ->
                val storyUiModels = stories.map { story ->
                    StoryUiModel(
                        id = story.id,
                        userId = story.userId,
                        userName = story.authorName.ifBlank { "Unknown" },
                        userAvatarUrl = story.authorAvatarUrl,
                        hasUnwatched = story.viewCount == 0
                    )
                }
                _uiState.update { it.copy(stories = storyUiModels) }
            }
        }
    }

    fun toggleLike(postId: String, reactionType: String? = null) {
        viewModelScope.launch {
            when (val result = feedRepository.toggleLike(postId, reactionType)) {
                is AppResult.Success -> { /* Optimistic update handled by DB Flow */ }
                is AppResult.Error -> {
                    timber.log.Timber.e("Failed to toggle like: ${result.error.message}")
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun refresh() {
        loadFeed(refresh = true)
        loadStories()
    }
}

/**
 * UI State for Feed screen
 */
data class FeedUiState(
    val posts: List<PostUiModel> = emptyList(),
    val stories: List<StoryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * UI Model for Post
 */
data class PostUiModel(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val contentText: String,
    val mediaUrls: List<String>,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val isLiked: Boolean,
    val reactionType: String?,
    val visibility: PostVisibility,
    val createdAt: String,
    val workoutSummary: WorkoutSummaryUi?
)

data class WorkoutSummaryUi(
    val type: String,
    val distance: String?,
    val duration: String,
    val calories: String
)

/**
 * UI Model for Story
 */
data class StoryUiModel(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val hasUnwatched: Boolean
)

/**
 * Map domain [Post] to [PostUiModel].
 */
fun Post.toUiModel(): PostUiModel {
    return PostUiModel(
        id = id,
        authorId = authorId,
        authorName = authorName.ifBlank { "Unknown User" },
        authorAvatarUrl = authorAvatarUrl,
        contentText = contentText,
        mediaUrls = mediaUrls,
        likes = likesCount,
        comments = commentsCount,
        shares = sharesCount,
        isLiked = isLiked,
        reactionType = null, // Domain Post doesn't track individual reaction type for current user
        visibility = visibility,
        createdAt = formatTimeAgo(createdAt.toEpochMilli()),
        workoutSummary = null // Workout info handled separately if needed
    )
}

fun formatDuration(millis: Long): String {
    val minutes = millis / 60000
    return "${minutes}m"
}

fun formatTimeAgo(instant: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - instant
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}