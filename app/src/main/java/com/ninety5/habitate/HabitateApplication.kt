package com.ninety5.habitate

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ninety5.habitate.core.utils.DebugLogger
import com.ninety5.habitate.worker.SyncWorker
import com.ninety5.habitate.worker.StoryCleanupWorker
import com.ninety5.habitate.core.glyph.HabitateGlyphManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.ninety5.habitate.worker.SyncScheduler

@HiltAndroidApp
class HabitateApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var glyphManager: HabitateGlyphManager
    @Inject lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize DebugLogger with application context
        DebugLogger.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(com.ninety5.habitate.core.utils.CrashlyticsTree())
        }
        
        // Initialize Glyph Manager
        glyphManager.init()
        
        scheduleSync()
        syncScheduler.scheduleArchivalWorker()
        StoryCleanupWorker.schedule(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        glyphManager.release()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "HabitateSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
