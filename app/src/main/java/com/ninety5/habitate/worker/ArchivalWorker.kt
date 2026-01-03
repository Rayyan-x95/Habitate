package com.ninety5.habitate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class ArchivalWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val postDao: PostDao,
    private val workoutDao: WorkoutDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val now = Instant.now()
            
            // Archive tasks completed > 30 days ago
            val taskCutoff = now.minus(30, ChronoUnit.DAYS)
            taskDao.archiveOldTasks(taskCutoff)

            // Archive posts > 1 year ago
            val postCutoff = now.minus(365, ChronoUnit.DAYS).toEpochMilli()
            postDao.archiveOldPosts(postCutoff)

            // Archive workouts > 1 year ago
            val workoutCutoff = now.minus(365, ChronoUnit.DAYS)
            workoutDao.archiveOldWorkouts(workoutCutoff)

            Result.success()
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Archival failed")
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
