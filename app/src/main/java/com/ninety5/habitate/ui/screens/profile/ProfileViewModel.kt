package com.ninety5.habitate.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.FeedRepository
import com.ninety5.habitate.data.repository.UserRepository
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.data.repository.MediaRepository
import com.ninety5.habitate.data.repository.UploadState
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
        
        val isCurrentUser = targetUserId == authRepository.getCurrentUserId()

        profileJob = viewModelScope.launch {
            // Combine user and posts flows for atomic state updates
            combine(
                userRepository.getUser(targetUserId),
                feedRepository.getPostsByUser(targetUserId)
            ) { user, postsWithAuthors ->
                val postUiModels = postsWithAuthors.map { it.toUiModel() }
                ProfileData(user, postUiModels)
            }
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .retryWhen { cause, attempt ->
                    // Retry up to 3 times with increasing delay for transient errors
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
                            isCurrentUser = isCurrentUser,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    private data class ProfileData(
        val user: UserEntity?,
        val posts: List<PostUiModel>
    )

    fun updateProfile(displayName: String, bio: String) {
        val currentUser = _uiState.value.user ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val updatedUser = currentUser.copy(
                    displayName = displayName,
                    bio = bio
                )
                userRepository.updateProfile(updatedUser)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to update profile") }
            }
        }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(saveSuccess = false, saveError = null) }
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
                        val currentUser = _uiState.value.user
                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(avatarUrl = state.url)
                            userRepository.updateProfile(updatedUser)
                        }
                        _uiState.update { it.copy(isUploading = false, uploadProgress = 0f) }
                    }
                    is UploadState.Error -> {
                        _uiState.update { 
                            it.copy(
                                isUploading = false, 
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
            val userId = authRepository.getCurrentUserId() ?: return@launch
            feedRepository.toggleLike(userId, postId, reactionType)
        }
    }
}

data class ProfileUiState(
    val user: UserEntity? = null,
    val isCurrentUser: Boolean = false,
    val posts: List<PostUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val uploadProgress: Float = 0f,
    val isUploading: Boolean = false
)
