package com.ninety5.habitate.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.ninety5.habitate.core.glyph.HabitateGlyphManager
import com.ninety5.habitate.core.utils.LiveNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroService : Service() {

    @Inject
    lateinit var liveNotificationManager: LiveNotificationManager
    
    @Inject
    lateinit var glyphManager: HabitateGlyphManager

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var timeRemainingInMillis = 0L
    private var totalDuration = 0L
    private var sessionCount = 1

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_SESSION_COUNT = "EXTRA_SESSION_COUNT"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 25 * 60 * 1000L)
                sessionCount = intent.getIntExtra(EXTRA_SESSION_COUNT, 1)
                startTimer(duration)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(duration: Long) {
        if (isRunning) return

        timeRemainingInMillis = duration
        totalDuration = duration
        isRunning = true

        // Build live notification with action buttons
        val notification = buildNotificationWithActions(timeRemainingInMillis, false)
        startForeground(LiveNotificationManager.NOTIFICATION_POMODORO, notification)
        
        // Glyph indicator for active focus session
        glyphManager.playPomodoroActive()

        timer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                updateNotification(false)
            }

            override fun onFinish() {
                isRunning = false
                onTimerComplete()
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        updateNotification(true)
    }

    private fun resumeTimer() {
        if (isRunning) return
        
        isRunning = true
        timer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                updateNotification(false)
            }

            override fun onFinish() {
                isRunning = false
                onTimerComplete()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        isRunning = false
        liveNotificationManager.cancel(LiveNotificationManager.NOTIFICATION_POMODORO)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerComplete() {
        // Glyph celebration for completed pomodoro
        glyphManager.playPomodoroComplete()
        
        // Show completion notification
        val completionNotification = liveNotificationManager.buildPomodoroNotification(
            timeRemaining = 0L,
            totalDuration = totalDuration,
            isPaused = false,
            sessionCount = sessionCount
        )
            .setContentTitle("✨ Focus Session Complete!")
            .setContentText("Great work! Take a break.")
            .setOngoing(false)
            .setAutoCancel(true)
        
        liveNotificationManager.notify(
            LiveNotificationManager.NOTIFICATION_POMODORO,
            completionNotification
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(isPaused: Boolean) {
        val notification = buildNotificationWithActions(timeRemainingInMillis, isPaused)
        liveNotificationManager.notify(
            LiveNotificationManager.NOTIFICATION_POMODORO,
            liveNotificationManager.buildPomodoroNotification(
                timeRemaining = timeRemainingInMillis,
                totalDuration = totalDuration,
                isPaused = isPaused,
                sessionCount = sessionCount
            ).apply {
                // Add action buttons
                addActions(this, isPaused)
            }
        )
    }

    private fun buildNotificationWithActions(millis: Long, isPaused: Boolean) =
        liveNotificationManager.buildPomodoroNotification(
            timeRemaining = millis,
            totalDuration = totalDuration,
            isPaused = isPaused,
            sessionCount = sessionCount
        ).apply {
            addActions(this, isPaused)
        }.build()

    private fun addActions(builder: androidx.core.app.NotificationCompat.Builder, isPaused: Boolean) {
        val pauseResumeIntent = Intent(this, PomodoroService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 1, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PomodoroService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Stop",
                stopPendingIntent
            )
    }
}
