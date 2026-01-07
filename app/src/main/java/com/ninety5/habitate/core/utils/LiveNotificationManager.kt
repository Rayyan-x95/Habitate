package com.ninety5.habitate.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ninety5.habitate.MainActivity
import com.ninety5.habitate.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages live updating notifications for the Habitate app.
 * Inspired by Android's Live Update pattern for ongoing activities.
 * 
 * Features:
 * - Real-time timer updates (Pomodoro, workouts)
 * - Progress tracking with custom layouts
 * - Action buttons for control
 * - Efficient battery-friendly updates
 * - Glyph visual feedback (Nothing phones)
 */
@Singleton
class LiveNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val glyphManager: com.ninety5.habitate.core.glyph.HabitateGlyphManager
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        // Notification Channels
        const val CHANNEL_POMODORO = "channel_pomodoro"
        const val CHANNEL_WORKOUT = "channel_workout"
        const val CHANNEL_HABIT_TRACKER = "channel_habit_tracker"
        
        // Notification IDs
        const val NOTIFICATION_POMODORO = 1001
        const val NOTIFICATION_WORKOUT = 1002
        const val NOTIFICATION_HABIT = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create all notification channels for live updates
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_POMODORO,
                    "Pomodoro Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows live Pomodoro timer countdown"
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHANNEL_WORKOUT,
                    "Workout Tracking",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows live workout stats and progress"
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHANNEL_HABIT_TRACKER,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Live habit tracking and streak updates"
                }
            )
            
            notificationManager.createNotificationChannels(channels)
        }
    }

    /**
     * Build a live notification for Pomodoro timer
     */
    fun buildPomodoroNotification(
        timeRemaining: Long,
        totalDuration: Long,
        isPaused: Boolean = false,
        sessionCount: Int = 0
    ): NotificationCompat.Builder {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        val progress = ((totalDuration - timeRemaining).toFloat() / totalDuration * 100).toInt()
        
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "focus/pomodoro")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(if (isPaused) "Pomodoro Paused" else "Focus Session")
            .setContentText(timeText)
            .setSubText("Session $sessionCount • ${progress}% complete")
            .setProgress(100, progress, false)
            .setOngoing(!isPaused)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(false)
    }

    /**
     * Build a live notification for workout tracking
     */
    fun buildWorkoutNotification(
        duration: Long,
        distance: Double? = null,
        calories: Int? = null,
        heartRate: Int? = null,
        workoutType: String = "Workout"
    ): NotificationCompat.Builder {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        
        val durationText = if (hours > 0) {
            String.format("%dh %02dm %02ds", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        
        val statsText = buildString {
            distance?.let { append("%.2f km".format(it)) }
            calories?.let { 
                if (isNotEmpty()) append(" • ")
                append("$it cal")
            }
            heartRate?.let {
                if (isNotEmpty()) append(" • ")
                append("💓 $it bpm")
            }
        }
        
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "workout/active")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_workout)
            .setContentTitle(workoutType)
            .setContentText(durationText)
            .setSubText(statsText)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis() - duration)
            .setShowWhen(true)
    }

    /**
     * Build a live notification for study session
     */
    fun buildHabitStreakNotification(
        habitName: String,
        currentStreak: Int,
        dailyProgress: Int,
        todayCompleted: Int,
        todayTotal: Int
    ): NotificationCompat.Builder {
        val progressText = "$todayCompleted/$todayTotal habits completed today"
        
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "tasks")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_HABIT_TRACKER)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("🔥 $currentStreak day streak!")
            .setContentText(habitName)
            .setSubText(progressText)
            .setProgress(100, dailyProgress, false)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    /**
     * Show or update a notification
     */
    fun notify(notificationId: Int, builder: NotificationCompat.Builder) {
        notificationManager.notify(notificationId, builder.build())
        
        // Trigger Glyph indicator for notifications (except ongoing timers)
        if (notificationId != NOTIFICATION_POMODORO && notificationId != NOTIFICATION_WORKOUT) {
            glyphManager.playNotificationIndicator()
        }
    }

    /**
     * Cancel a notification
     */
    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAll() {
        notificationManager.cancelAll()
    }

    /**
     * Format time for display
     */
    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
}
