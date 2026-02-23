package com.ninety5.habitate.domain.repository

import android.net.Uri
import com.ninety5.habitate.domain.model.UploadState
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for media upload operations.
 */
interface MediaRepository {
    fun uploadMedia(uri: Uri): Flow<UploadState>
}
