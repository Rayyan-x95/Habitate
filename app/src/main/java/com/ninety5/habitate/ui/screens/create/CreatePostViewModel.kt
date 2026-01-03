package com.ninety5.habitate.ui.screens.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.FeedRepository
import com.ninety5.habitate.data.repository.MediaRepository
import com.ninety5.habitate.data.repository.UploadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val selectedImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadProgress: Float = 0f,
    val uploadedImageUrl: String? = null,
    val visibility: Visibility = Visibility.PRIVATE
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val authRepository: AuthRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun setVisibility(visibility: Visibility) {
        _uiState.update { it.copy(visibility = visibility) }
    }

    fun setSelectedImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, uploadedImageUrl = null) }
    }

    fun removeSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null, uploadedImageUrl = null) }
    }

    fun createPost(content: String) {
        if (content.isBlank() && _uiState.value.uploadedImageUrl == null) return

        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Offline-first: Use local URI directly. SyncWorker will handle upload.
                val mediaUrls = if (_uiState.value.selectedImageUri != null) {
                    listOf(_uiState.value.selectedImageUri.toString())
                } else {
                    emptyList()
                }

                val post = PostEntity(
                    id = UUID.randomUUID().toString(),
                    authorId = userId,
                    contentText = content,
                    mediaUrls = mediaUrls,
                    visibility = _uiState.value.visibility,
                    habitatId = null,
                    workoutId = null,
                    syncState = SyncState.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                feedRepository.createPost(post)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
