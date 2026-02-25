package com.ninety5.habitate.ui.screens.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.PostVisibility
import com.ninety5.habitate.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val selectedImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadProgress: Float = 0f,
    val uploadedImageUrl: String? = null,
    val visibility: PostVisibility = PostVisibility.PRIVATE
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun setVisibility(visibility: PostVisibility) {
        _uiState.update { it.copy(visibility = visibility) }
    }

    fun setSelectedImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, uploadedImageUrl = null) }
    }

    fun removeSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null, uploadedImageUrl = null) }
    }

    fun createPost(content: String) {
        if (content.isBlank() && _uiState.value.selectedImageUri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Offline-first: Use local URI directly. SyncWorker will handle upload.
            val mediaUrls = if (_uiState.value.selectedImageUri != null) {
                listOf(_uiState.value.selectedImageUri.toString())
            } else {
                emptyList()
            }

            when (val result = feedRepository.createPost(
                text = content,
                mediaUrls = mediaUrls,
                visibility = _uiState.value.visibility,
                habitatId = null
            )) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    Timber.e("Failed to create post: ${result.error.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
