package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.NotificationDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.NotificationEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService
) {

    fun getAllNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications(userId)
    }

    suspend fun refreshNotifications(): Result<Unit> {
        return try {
            val dtos = apiService.getNotifications()
            val entities = dtos.map { it.toEntity() }
            notificationDao.upsertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
        
        val syncOp = SyncOperationEntity(
            entityType = "notification_read",
            entityId = notificationId,
            operation = "UPDATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
        
        val syncOp = SyncOperationEntity(
            entityType = "notification_read_all",
            entityId = userId,
            operation = "UPDATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }
}
