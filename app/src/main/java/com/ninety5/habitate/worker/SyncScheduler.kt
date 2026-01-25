package com.ninety5.habitate.worker

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules periodic background sync of offline changes.
 * 
 * - Syncs every 15 minutes when connected to network
 * - Requires network connectivity
 * - Persists across app restarts
 * - Respects battery optimization settings
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SYNC_WORK_NAME = "habitate_periodic_sync"
        private const val SYNC_INTERVAL_MINUTES = 15L
    }
    
    /**
     * Schedule periodic sync worker.
     * Safe to call multiple times - will replace existing schedule.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // Don't replace if already scheduled
                syncRequest
            )
    }

    fun scheduleArchivalWorker() {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val archivalRequest = PeriodicWorkRequestBuilder<ArchivalWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "habitate_archival_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                archivalRequest
            )
    }
    
    /**
     * Trigger immediate one-time sync (e.g., on user action).
     * Useful for pull-to-refresh or manual sync button.
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "immediate_sync",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }
    
    /**
     * Cancel periodic sync (e.g., when user logs out).
     */
    fun cancelPeriodicSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Check if sync worker is currently running.
     */
    @Suppress("RestrictedApi")
    suspend fun isSyncInProgress(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
            .await()
        
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
}
