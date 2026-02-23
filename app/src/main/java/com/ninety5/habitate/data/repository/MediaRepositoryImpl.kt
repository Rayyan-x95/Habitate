package com.ninety5.habitate.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ninety5.habitate.worker.UploadWorker
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun uploadMedia(uri: Uri): Flow<com.ninety5.habitate.domain.model.UploadState> {
        val workRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf(UploadWorker.KEY_URI to uri.toString()))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(workRequest)

        return workManager.getWorkInfoByIdLiveData(workRequest.id)
            .asFlow()
            .map { workInfo ->
                if (workInfo == null) return@map com.ninety5.habitate.domain.model.UploadState.Progress(0f)

                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> com.ninety5.habitate.domain.model.UploadState.Progress(0f)
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getFloat(UploadWorker.KEY_PROGRESS, 0f)
                        com.ninety5.habitate.domain.model.UploadState.Progress(progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val url = workInfo.outputData.getString(UploadWorker.KEY_RESULT_URL)
                        if (url != null) {
                            com.ninety5.habitate.domain.model.UploadState.Success(url)
                        } else {
                            com.ninety5.habitate.domain.model.UploadState.Error(Exception("Upload succeeded but no URL returned"))
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        com.ninety5.habitate.domain.model.UploadState.Error(Exception("Upload failed"))
                    }
                    WorkInfo.State.CANCELLED -> {
                        com.ninety5.habitate.domain.model.UploadState.Error(Exception("Upload cancelled"))
                    }
                }
            }
    }
}
