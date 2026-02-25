package com.ninety5.habitate.ui.screens.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Comment
import com.ninety5.habitate.domain.model.Post
import com.ninety5.habitate.domain.model.User
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.CommentRepository
import com.ninety5.habitate.domain.repository.FeedRepository
import com.ninety5.habitate.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val author: User? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _postId = MutableStateFlow<String>(checkNotNull(savedStateHandle["postId"]))

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _postId.flatMapLatest { id ->
                feedRepository.observePost(id)
            }.collectLatest { post ->
                if (post != null) {
                    _uiState.update {
                        it.copy(
                            post = post,
                            isLoading = false,
                            isLiked = post.isLiked,
                            likeCount = post.likesCount,
                            commentCount = post.commentsCount
                        )
                    }
                    loadAuthor(post.authorId)
                } else {
                    _uiState.update { it.copy(error = "Post not found", isLoading = false) }
                }
            }
        }

        viewModelScope.launch {
            _postId.flatMapLatest { id ->
                commentRepository.getCommentsForPost(id)
            }.collectLatest { comments ->
                _uiState.update { it.copy(comments = comments) }
            }
        }
    }

    fun loadPost(id: String) {
        if (_postId.value != id) {
            _uiState.value = PostDetailUiState(isLoading = true)
            _postId.value = id
        }
    }

    private fun loadAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                userRepository.observeUser(authorId).collect { user ->
                    _uiState.update { it.copy(author = user) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load author")
            }
        }
    }

    fun toggleLike(reactionType: String? = null) {
        viewModelScope.launch {
            val currentlyLiked = _uiState.value.isLiked
            val currentLikeCount = _uiState.value.likeCount
            // Optimistic update
            _uiState.update {
                it.copy(
                    isLiked = !currentlyLiked,
                    likeCount = if (currentlyLiked) it.likeCount - 1 else it.likeCount + 1
                )
            }
            when (val result = feedRepository.toggleLike(_postId.value, reactionType)) {
                is AppResult.Success -> { /* DB Flow will confirm */ }
                is AppResult.Error -> {
                    Timber.e("Failed to toggle like: ${result.error.message}")
                    // Rollback to captured prior state
                    _uiState.update {
                        it.copy(
                            isLiked = currentlyLiked,
                            likeCount = currentLikeCount
                        )
                    }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun addComment(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                commentRepository.createComment(_postId.value, content)
                _uiState.update { it.copy(commentCount = it.commentCount + 1) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to add comment")
                _uiState.update { it.copy(error = "Failed to add comment") }
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(commentId)
                _uiState.update { it.copy(commentCount = maxOf(0, it.commentCount - 1)) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete comment")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
