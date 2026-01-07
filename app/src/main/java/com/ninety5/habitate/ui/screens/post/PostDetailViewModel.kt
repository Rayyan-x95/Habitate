package com.ninety5.habitate.ui.screens.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.dao.CommentWithUser
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.CommentRepository
import com.ninety5.habitate.data.repository.FeedRepository
import com.ninety5.habitate.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PostDetailUiState(
    val post: PostEntity? = null,
    val author: UserEntity? = null,
    val comments: List<CommentWithUser> = emptyList(),
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

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPost()
        loadComments()
    }

    private fun loadPost() {
        viewModelScope.launch {
            try {
                feedRepository.getPostById(postId).collect { post ->
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
                        // Load author info
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
                userRepository.getUser(authorId).collect { user ->
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
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val currentlyLiked = _uiState.value.isLiked
                // Optimistic update
                _uiState.update {
                    it.copy(
                        isLiked = !currentlyLiked,
                        likeCount = if (currentlyLiked) it.likeCount - 1 else it.likeCount + 1
                    )
                }
                feedRepository.toggleLike(userId, postId, reactionType)
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle like")
                // Rollback
                _uiState.update {
                    it.copy(
                        isLiked = !it.isLiked,
                        likeCount = if (it.isLiked) it.likeCount - 1 else it.likeCount + 1
                    )
                }
            }
        }
    }

    fun addComment(content: String) {
        if (content.isBlank()) return
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                commentRepository.createComment(userId, postId, content)
                _uiState.update { it.copy(commentCount = it.commentCount + 1) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to add comment")
                _uiState.update { it.copy(error = "Failed to add comment") }
            }
        }
    }

    fun deleteComment(commentId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(commentId, userId)
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
