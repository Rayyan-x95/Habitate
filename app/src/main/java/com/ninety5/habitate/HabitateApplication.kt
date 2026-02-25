package com.ninety5.habitate

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ninety5.habitate.core.auth.SessionManager
import com.ninety5.habitate.worker.StoryCleanupWorker
import com.ninety5.habitate.worker.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HabitateApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(com.ninety5.habitate.core.utils.CrashlyticsTree())
        }
        syncScheduler.schedulePeriodicSync()
        syncScheduler.scheduleArchivalWorker()
        StoryCleanupWorker.schedule(this)
        sessionManager.startObserving()
    }
}
