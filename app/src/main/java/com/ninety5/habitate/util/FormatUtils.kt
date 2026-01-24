package com.ninety5.habitate.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility functions for formatting dates, times, and durations.
 * Consolidates duplicate formatting logic used across the app.
 */
object FormatUtils {

    /**
     * Formats a duration in milliseconds to a human-readable string.
     * @param millis Duration in milliseconds
     * @return Formatted string (e.g., "1h 30m", "45m", "30s")
     */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    /**
     * Formats a duration for workout display (mm:ss format).
     * @param millis Duration in milliseconds
     * @return Formatted string (e.g., "01:30:45", "45:30")
     */
    fun formatDurationClock(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats a timestamp as a relative time ago string.
     * @param instant Timestamp in milliseconds since epoch
     * @return Relative time string (e.g., "Just now", "5m ago", "2h ago", "3d ago")
     */
    fun formatTimeAgo(instant: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - instant

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> {
                val date = Instant.ofEpochMilli(instant)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                formatDate(date)
            }
        }
    }

    /**
     * Formats a LocalDate to a readable date string.
     * @param date The date to format
     * @return Formatted date string (e.g., "Jan 15", "Dec 31, 2024")
     */
    fun formatDate(date: LocalDate): String {
        val now = LocalDate.now()
        val formatter = if (date.year == now.year) {
            DateTimeFormatter.ofPattern("MMM d")
        } else {
            DateTimeFormatter.ofPattern("MMM d, yyyy")
        }
        return date.format(formatter)
    }

    /**
     * Formats a time for display in 12-hour format.
     * @param time The LocalDateTime to format
     * @return Formatted time string (e.g., "2:30 PM")
     */
    fun formatTime(time: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return time.format(formatter)
    }

    /**
     * Formats a timestamp for focus/pomodoro timer display.
     * @param seconds Remaining seconds
     * @return Formatted timer string (e.g., "25:00", "05:30")
     */
    fun formatTimerSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, secs)
    }

    /**
     * Formats a number with appropriate suffixes for social display.
     * Uses Locale.US to ensure consistent formatting (e.g., "1.2K" not "1,2K").
     * @param count The number to format
     * @return Formatted string (e.g., "1.2K", "5M", "999")
     */
    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
