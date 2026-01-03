package com.ninety5.habitate.core.focus

import android.app.NotificationManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import com.ninety5.habitate.data.local.dao.FocusDao
import com.ninety5.habitate.data.local.entity.FocusSessionEntity
import com.ninety5.habitate.data.local.entity.FocusSessionStatus
import com.ninety5.habitate.data.local.entity.SyncState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusDao: FocusDao
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSession(userId: String, durationSeconds: Long, soundTrack: String? = null) {
        scope.launch {
            val session = FocusSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                startTime = Instant.now(),
                endTime = null,
                durationSeconds = durationSeconds,
                status = FocusSessionStatus.IN_PROGRESS,
                soundTrack = soundTrack,
                syncState = SyncState.PENDING,
                updatedAt = Instant.now()
            )
            focusDao.upsert(session)
            enableDnd()
        }
    }

    fun stopSession(rating: Int? = null) {
        scope.launch {
            val current = focusDao.getCurrentSession() ?: return@launch
            val updated = current.copy(
                endTime = Instant.now(),
                status = FocusSessionStatus.COMPLETED,
                rating = rating,
                syncState = SyncState.PENDING,
                updatedAt = Instant.now()
            )
            focusDao.upsert(updated)
            disableDnd()
        }
    }

    fun abortSession() {
        scope.launch {
            val current = focusDao.getCurrentSession() ?: return@launch
            val updated = current.copy(
                endTime = Instant.now(),
                status = FocusSessionStatus.ABORTED,
                syncState = SyncState.PENDING,
                updatedAt = Instant.now()
            )
            focusDao.upsert(updated)
            disableDnd()
        }
    }

    private fun enableDnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            }
        }
    }

    private fun disableDnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    fun getRecentAppUsage(): List<UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 // Last 1 minute
        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
    }
}
