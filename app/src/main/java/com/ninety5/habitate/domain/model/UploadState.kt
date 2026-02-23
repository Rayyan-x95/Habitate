package com.ninety5.habitate.domain.model

/**
 * Domain model representing the state of a media upload.
 */
sealed class UploadState {
    data class Progress(val progress: Float) : UploadState()
    data class Success(val url: String) : UploadState()
    data class Error(val exception: Exception) : UploadState()
}
