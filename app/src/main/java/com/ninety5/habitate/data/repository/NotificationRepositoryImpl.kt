package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.NotificationDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.Notification
import com.ninety5.habitate.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) : NotificationRepository {

    private val userId: String
        get() = securePreferences.userId
            ?: error("userId must not be null — user is not authenticated")

    override fun observeUnreadCount(): Flow<Int> {
        return notificationDao.getAllNotifications(userId).map { list ->
            list.count { !it.isRead }
        }
    }

    override fun observeNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun markAsRead(notificationId: String): AppResult<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "notification_read",
                    entityId = notificationId,
                    operation = "UPDATE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark notification as read: $notificationId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun markAllAsRead(): AppResult<Unit> {
        return try {
            notificationDao.markAllAsRead(userId)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "notification_read_all",
                    entityId = userId,
                    operation = "UPDATE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark all notifications as read")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun deleteNotification(notificationId: String): AppResult<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "notification",
                    entityId = notificationId,
                    operation = "DELETE",
                    payload = "{}",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete notification: $notificationId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun refreshNotifications(): AppResult<Unit> {
        return try {
            val dtos = apiService.getNotifications()
            val entities = dtos.map { it.toEntity() }
            notificationDao.upsertAll(entities)
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh notifications")
            AppResult.Error(AppError.from(e))
        }
    }
}
