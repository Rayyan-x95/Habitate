package com.ninety5.habitate.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.ProgressRequestBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import timber.log.Timber

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString(KEY_URI) ?: return Result.failure()
        val uri = Uri.parse(uriString)

        return try {
            val file = getFileFromUri(uri) ?: return Result.failure()
            
            setProgress(workDataOf(KEY_PROGRESS to 0f))

            var lastUpdate = 0L
            val requestBody = ProgressRequestBody(file, "image/*") { progress ->
                val now = System.currentTimeMillis()
                if (now - lastUpdate > 500 || progress >= 1.0f) { // Throttle updates to every 500ms
                    lastUpdate = now
                    setProgressAsync(workDataOf(KEY_PROGRESS to progress))
                }
            }

            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val url = apiService.uploadMedia(part)

            Result.success(workDataOf(KEY_RESULT_URL to url))
        } catch (e: Exception) {
            Timber.e(e, "Upload failed")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = applicationContext.contentResolver.openInputStream(uri) ?: return null
            val file = File(applicationContext.cacheDir, "upload_${System.currentTimeMillis()}")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to create file from URI")
            null
        }
    }

    companion object {
        const val KEY_URI = "uri"
        const val KEY_PROGRESS = "progress"
        const val KEY_RESULT_URL = "result_url"
    }
}
