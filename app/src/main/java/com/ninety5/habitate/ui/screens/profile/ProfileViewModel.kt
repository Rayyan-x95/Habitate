package com.ninety5.habitate.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.User
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.FeedRepository
import com.ninety5.habitate.domain.repository.UserRepository
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.domain.repository.MediaRepository
import com.ninety5.habitate.domain.model.UploadState
import android.net.Uri
import com.ninety5.habitate.ui.screens.feed.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    // If userId is passed in arguments, use it. Otherwise use current logged in user.
    private val argUserId: String? = savedStateHandle["userId"]
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private var profileJob: Job? = null
    private var followObserverJob: Job? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        profileJob?.cancel()
        
        val targetUserId = argUserId ?: authRepository.getCurrentUserId()
        
        if (targetUserId == null) {
            _uiState.update { it.copy(isLoading = false, error = "User not found") }
            return
        }
        
        val currentUserId = authRepository.getCurrentUserId()
        val isCurrentUser = targetUserId == currentUserId

        profileJob = viewModelScope.launch {
            // Combine user, posts, and counts for atomic state updates
            combine(
                userRepository.observeUser(targetUserId),
                feedRepository.getPostsByUser(targetUserId),
                userRepository.observeFollowerCount(targetUserId),
                userRepository.observeFollowingCount(targetUserId)
            ) { user, postsWithAuthors, followerCount, followingCount ->
                val postUiModels = postsWithAuthors.map { it.toUiModel() }
                ProfileData(user, postUiModels, followerCount, followingCount)
            }
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .retryWhen { cause, attempt ->
                    if (attempt < 3 && cause !is kotlinx.coroutines.CancellationException) {
                        Timber.w(cause, "Profile load failed, retrying (attempt $attempt)")
                        kotlinx.coroutines.delay(1000L * (attempt + 1))
                        true
                    } else {
                        false
                    }
                }
                .catch { e ->
                    Timber.e(e, "Failed to load profile")
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load profile") }
                }
                .collect { data ->
                    _uiState.update { 
                        it.copy(
                            user = data.user, 
                            posts = data.posts,
                            followerCount = data.followerCount,
                            followingCount = data.followingCount,
                            isCurrentUser = isCurrentUser,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }

        // Observe follow state for other users
        if (!isCurrentUser && currentUserId != null) {
            followObserverJob?.cancel()
            followObserverJob = viewModelScope.launch {
                userRepository.isFollowing(currentUserId, targetUserId)
                    .catch { Timber.e(it, "Failed to observe follow state") }
                    .collect { following ->
                        _uiState.update { it.copy(isFollowing = following) }
                    }
            }
        }
    }
    
    private data class ProfileData(
        val user: User?,
        val posts: List<PostUiModel>,
        val followerCount: Int,
        val followingCount: Int
    )

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            when (val result = userRepository.updateProfile(displayName, bio, null)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is AppResult.Error -> {
                    Timber.e("Failed to update profile: ${result.error.message}")
                    _uiState.update { it.copy(isSaving = false, saveError = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun followUser() {
        val targetUserId = argUserId ?: return
        viewModelScope.launch {
            when (val result = userRepository.followUser(targetUserId)) {
                is AppResult.Success -> { /* Optimistic update handled by Flow */ }
                is AppResult.Error -> {
                    Timber.e("Failed to follow user: ${result.error.message}")
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun unfollowUser() {
        val targetUserId = argUserId ?: return
        viewModelScope.launch {
            when (val result = userRepository.unfollowUser(targetUserId)) {
                is AppResult.Success -> { /* Optimistic update handled by Flow */ }
                is AppResult.Error -> {
                    Timber.e("Failed to unfollow user: ${result.error.message}")
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(saveSuccess = false, saveError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f) }
            
            mediaRepository.uploadMedia(uri).collect { state ->
                when (state) {
                    is UploadState.Progress -> {
                        _uiState.update { it.copy(uploadProgress = state.progress) }
                    }
                    is UploadState.Success -> {
                        // Update user profile with new avatar URL
                        when (val result = userRepository.updateProfile(null, null, state.url)) {
                            is AppResult.Success -> {
                                _uiState.update { it.copy(isUploading = false, uploadProgress = 0f) }
                            }
                            is AppResult.Error -> {
                                _uiState.update {
                                    it.copy(isUploading = false, saveError = "Upload succeeded but failed to save: ${result.error.message}")
                                }
                            }
                            is AppResult.Loading -> { /* no-op */ }
                        }
                    }
                    is UploadState.Error -> {
                        _uiState.update { 
                            it.copy(
                                isUploading = false,
                                uploadProgress = 0f,
                                saveError = "Upload failed: ${state.exception.message}"
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun toggleLike(postId: String, reactionType: String? = null) {
        viewModelScope.launch {
            when (val result = feedRepository.toggleLike(postId, reactionType)) {
                is AppResult.Success -> { /* Optimistic update handled by DB Flow */ }
                is AppResult.Error -> {
                    Timber.e("Failed to toggle like: ${result.error.message}")
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val posts: List<PostUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val uploadProgress: Float = 0f,
    val isUploading: Boolean = false
)
