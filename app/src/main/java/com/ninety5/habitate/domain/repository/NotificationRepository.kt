package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for notification operations.
 */
interface NotificationRepository {
    fun observeUnreadCount(): Flow<Int>
    fun observeNotifications(): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String): AppResult<Unit>
    suspend fun markAllAsRead(): AppResult<Unit>
    suspend fun deleteNotification(notificationId: String): AppResult<Unit>
    suspend fun refreshNotifications(): AppResult<Unit>
}
