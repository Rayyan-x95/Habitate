package com.ninety5.habitate.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ninety5.habitate.data.repository.FeedRepository
import com.ninety5.habitate.data.repository.StoryRepository
import com.ninety5.habitate.data.repository.PostWithAuthor
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.Visibility
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the Feed screen.
 * Manages feed state including posts, stories, and user interactions.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val storyRepository: StoryRepository,
    private val authRepository: com.ninety5.habitate.data.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    val pagingDataFlow: Flow<PagingData<PostUiModel>> = feedRepository.getFeedPostsPaging()
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
                _uiState.update { it.copy(isRefreshing = true) }
                launch { feedRepository.refreshFeed() }
                launch { storyRepository.refreshStories() }
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getActiveStories().collect { stories ->
                val storyUiModels = stories.map { storyWithUser ->
                    StoryUiModel(
                        id = storyWithUser.story.id,
                        userId = storyWithUser.story.userId,
                        userName = storyWithUser.user?.displayName ?: "Unknown",
                        userAvatarUrl = storyWithUser.user?.avatarUrl,
                        hasUnwatched = true // Logic for watched state can be added later
                    )
                }
                _uiState.update { it.copy(stories = storyUiModels) }
            }
        }
    }

    fun toggleLike(postId: String, reactionType: String? = null) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            feedRepository.toggleLike(userId, postId, reactionType)
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
    val visibility: Visibility,
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


fun PostWithAuthor.toUiModel(): PostUiModel {
    return PostUiModel(
        id = post.id,
        authorId = post.authorId,
        authorName = author?.displayName ?: "Unknown User",
        authorAvatarUrl = author?.avatarUrl,
        contentText = post.contentText ?: "",
        mediaUrls = post.mediaUrls,
        likes = post.likesCount,
        comments = post.commentsCount,
        shares = post.sharesCount,
        isLiked = post.isLiked,
        reactionType = post.reactionType,
        visibility = post.visibility,
        createdAt = formatTimeAgo(post.createdAt),
        workoutSummary = workout?.let {
            WorkoutSummaryUi(
                type = it.type,
                distance = it.distanceMeters?.let { m -> "${(m / 1000.0)} km" },
                duration = formatDuration(it.endTs.toEpochMilli() - it.startTs.toEpochMilli()),
                calories = it.calories?.let { c -> "${c.toInt()} kcal" } ?: ""
            )
        }
    )
}

fun formatDuration(millis: Long): String {
    val minutes = millis / 60000
    return "${minutes}m"
}

fun formatTimeAgo(instant: Long): String {
    // Simple implementation, can be improved
    val now = System.currentTimeMillis()
    val diff = now - instant
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}