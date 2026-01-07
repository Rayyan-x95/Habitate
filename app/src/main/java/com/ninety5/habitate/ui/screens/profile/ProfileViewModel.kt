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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val targetUserId = argUserId ?: authRepository.getCurrentUserId()
            
            if (targetUserId == null) {
                _uiState.update { it.copy(error = "User not found") }
                return@launch
            }

            // Load User
            launch {
                userRepository.getUser(targetUserId).collect { user ->
                    _uiState.update { it.copy(user = user, isCurrentUser = targetUserId == authRepository.getCurrentUserId()) }
                }
            }

            // Load Posts
            launch {
                feedRepository.getPostsByUser(targetUserId).collect { postsWithAuthors ->
                    val postUiModels = postsWithAuthors.map { it.toUiModel() }
                    _uiState.update { it.copy(posts = postUiModels) }
                }
            }
        }
    }

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
