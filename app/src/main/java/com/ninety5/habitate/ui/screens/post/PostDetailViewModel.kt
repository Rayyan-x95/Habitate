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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPost()
        loadComments()
    }

    fun loadPost(id: String) {
        if (postId != id) {
            postId = id
            loadPost()
            loadComments()
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            try {
                feedRepository.observePost(postId).collect { post ->
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
                        // Author info is available directly from Post domain model,
                        // but we also observe full User for profile nav etc.
                        loadAuthor(post.authorId)
                    } else {
                        _uiState.update { it.copy(error = "Post not found", isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load post")
                _uiState.update { it.copy(error = "Failed to load post: ${e.message}", isLoading = false) }
            }
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

    private fun loadComments() {
        viewModelScope.launch {
            try {
                commentRepository.getCommentsForPost(postId).collect { comments ->
                    _uiState.update { it.copy(comments = comments) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load comments")
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
            when (val result = feedRepository.toggleLike(postId, reactionType)) {
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
                commentRepository.createComment(postId, content)
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
