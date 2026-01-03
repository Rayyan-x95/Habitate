package com.ninety5.habitate.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ninety5.habitate.core.utils.LiveNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Service for tracking workouts with live updating notifications.
 * Shows real-time stats: duration, distance, calories, heart rate.
 */
@AndroidEntryPoint
class WorkoutTrackingService : Service() {

    @Inject
    lateinit var liveNotificationManager: LiveNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var updateJob: Job? = null
    
    private var isTracking = false
    private var startTime = 0L
    private var pausedTime = 0L
    private var totalPausedDuration = 0L
    
    // Workout stats
    private var distance = 0.0 // in kilometers
    private var calories = 0
    private var heartRate = 0
    private var workoutType = "Workout"

    companion object {
        const val ACTION_START = "ACTION_START_WORKOUT"
        const val ACTION_PAUSE = "ACTION_PAUSE_WORKOUT"
        const val ACTION_RESUME = "ACTION_RESUME_WORKOUT"
        const val ACTION_STOP = "ACTION_STOP_WORKOUT"
        const val ACTION_UPDATE_STATS = "ACTION_UPDATE_STATS"
        
        const val EXTRA_WORKOUT_TYPE = "EXTRA_WORKOUT_TYPE"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_CALORIES = "EXTRA_CALORIES"
        const val EXTRA_HEART_RATE = "EXTRA_HEART_RATE"
        
        const val UPDATE_INTERVAL = 1000L // Update every second
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "Workout"
                startTracking()
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
            ACTION_UPDATE_STATS -> {
                distance = intent.getDoubleExtra(EXTRA_DISTANCE, distance)
                calories = intent.getIntExtra(EXTRA_CALORIES, calories)
                heartRate = intent.getIntExtra(EXTRA_HEART_RATE, heartRate)
                updateNotification()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking) return
        
        isTracking = true
        startTime = System.currentTimeMillis()
        totalPausedDuration = 0L
        
        val notification = buildNotificationWithActions()
        startForeground(LiveNotificationManager.NOTIFICATION_WORKOUT, notification.build())
        
        startUpdates()
    }

    private fun pauseTracking() {
        if (!isTracking) return
        
        isTracking = false
        pausedTime = System.currentTimeMillis()
        updateJob?.cancel()
        updateNotification()
    }

    private fun resumeTracking() {
        if (isTracking) return
        
        isTracking = true
        totalPausedDuration += System.currentTimeMillis() - pausedTime
        
        startUpdates()
    }

    private fun stopTracking() {
        isTracking = false
        updateJob?.cancel()
        
        // Show completion notification
        val duration = getCurrentDuration()
        val completionNotification = liveNotificationManager.buildWorkoutNotification(
            duration = duration,
            distance = if (distance > 0) distance else null,
            calories = if (calories > 0) calories else null,
            heartRate = null, // Don't show heart rate in completion
            workoutType = workoutType
        )
            .setContentTitle("$workoutType Complete! 🎉")
            .setContentText(formatDuration(duration))
            .setOngoing(false)
            .setAutoCancel(true)
            .clearActions() // Remove pause/stop buttons
        
        liveNotificationManager.notify(
            LiveNotificationManager.NOTIFICATION_WORKOUT,
            completionNotification
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startUpdates() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isTracking) {
                updateNotification()
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private fun updateNotification() {
        val duration = getCurrentDuration()
        val notification = buildNotificationWithActions()
        liveNotificationManager.notify(
            LiveNotificationManager.NOTIFICATION_WORKOUT,
            notification
        )
    }

    private fun buildNotificationWithActions() =
        liveNotificationManager.buildWorkoutNotification(
            duration = getCurrentDuration(),
            distance = if (distance > 0) distance else null,
            calories = if (calories > 0) calories else null,
            heartRate = if (heartRate > 0) heartRate else null,
            workoutType = workoutType
        ).apply {
            addActions(this)
        }

    private fun addActions(builder: androidx.core.app.NotificationCompat.Builder) {
        val pauseResumeIntent = Intent(this, WorkoutTrackingService::class.java).apply {
            action = if (isTracking) ACTION_PAUSE else ACTION_RESUME
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 1, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WorkoutTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder
            .addAction(
                if (isTracking) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isTracking) "Pause" else "Resume",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Finish",
                stopPendingIntent
            )
    }

    private fun getCurrentDuration(): Long {
        if (!isTracking && pausedTime > 0) {
            return pausedTime - startTime - totalPausedDuration
        }
        return System.currentTimeMillis() - startTime - totalPausedDuration
    }

    private fun formatDuration(millis: Long): String {
        val hours = (millis / (1000 * 60 * 60)).toInt()
        val minutes = ((millis / (1000 * 60)) % 60).toInt()
        val seconds = ((millis / 1000) % 60).toInt()
        
        return if (hours > 0) {
            String.format("%dh %02dm %02ds", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }
}
