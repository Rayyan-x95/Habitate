package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.HabitateDatabase
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.TaskEntity
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.Task
import com.ninety5.habitate.domain.model.TaskPriority
import com.ninety5.habitate.domain.model.TaskStatus
import com.ninety5.habitate.domain.repository.TaskRepository
import com.squareup.moshi.Moshi
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi,
    private val securePreferences: SecurePreferences,
    private val database: HabitateDatabase
) : TaskRepository {

    override fun observeAllTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTasksByStatus(status: String): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities
                .map { it.toDomain() }
                .filter { it.status.name.equals(status, ignoreCase = true) }
        }
    }

    override suspend fun getTask(taskId: String): AppResult<Task> {
        return try {
            val entity = taskDao.getTaskById(taskId)
                ?: return AppResult.Error(AppError.NotFound("Task not found"))
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get task: $taskId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun createTask(task: Task): AppResult<Task> {
        return try {
            val now = Instant.now()
            val id = task.id.ifBlank { UUID.randomUUID().toString() }
            val entity = task.copy(id = id, createdAt = now, updatedAt = now).toEntity(SyncState.PENDING)

            database.withTransaction {
                taskDao.upsert(entity)
                val payload = moshi.adapter(TaskEntity::class.java).toJson(entity)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "task",
                        entityId = id,
                        operation = "CREATE",
                        payload = payload,
                        status = SyncStatus.PENDING,
                        createdAt = now,
                        lastAttemptAt = null
                    )
                )
            }
            
            // Attempt immediate sync
            try {
                val payload = moshi.adapter(TaskEntity::class.java).toJson(entity)
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload)
                apiService.create("tasks", requestBody)
                taskDao.updateSyncState(id, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("task", id, "CREATE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for task $id, will retry later")
            }
            
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to create task")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun updateTask(task: Task): AppResult<Unit> {
        return try {
            val now = Instant.now()
            val entity = task.copy(updatedAt = now).toEntity(SyncState.PENDING)

            database.withTransaction {
                taskDao.upsert(entity)
                val payload = moshi.adapter(TaskEntity::class.java).toJson(entity)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "task",
                        entityId = task.id,
                        operation = "UPDATE",
                        payload = payload,
                        status = SyncStatus.PENDING,
                        createdAt = now,
                        lastAttemptAt = null
                    )
                )
            }
            
            // Attempt immediate sync
            try {
                val payload = moshi.adapter(TaskEntity::class.java).toJson(entity)
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload)
                apiService.update("tasks", task.id, requestBody)
                taskDao.updateSyncState(task.id, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("task", task.id, "UPDATE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for task ${task.id}, will retry later")
            }
            
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update task: ${task.id}")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun deleteTask(taskId: String): AppResult<Unit> {
        return try {
            database.withTransaction {
                taskDao.updateStatus(
                    taskId,
                    com.ninety5.habitate.data.local.entity.TaskStatus.ARCHIVED,
                    System.currentTimeMillis()
                )
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "task",
                        entityId = taskId,
                        operation = "UPDATE",
                        payload = """{"status": "ARCHIVED"}""",
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }
            
            // Attempt immediate sync
            try {
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), """{"status": "ARCHIVED"}""")
                apiService.update("tasks", taskId, requestBody)
                taskDao.updateSyncState(taskId, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("task", taskId, "UPDATE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for task $taskId, will retry later")
            }
            
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete task: $taskId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun completeTask(taskId: String): AppResult<Unit> {
        return try {
            taskDao.updateStatus(
                taskId,
                com.ninety5.habitate.data.local.entity.TaskStatus.DONE,
                System.currentTimeMillis()
            )
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "task",
                    entityId = taskId,
                    operation = "UPDATE",
                    payload = """{"status": "DONE"}""",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to complete task: $taskId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun archiveTask(taskId: String): AppResult<Unit> {
        return try {
            taskDao.updateStatus(
                taskId,
                com.ninety5.habitate.data.local.entity.TaskStatus.ARCHIVED,
                System.currentTimeMillis()
            )
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "task",
                    entityId = taskId,
                    operation = "UPDATE",
                    payload = """{"status": "ARCHIVED"}""",
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to archive task: $taskId")
            AppResult.Error(AppError.from(e))
        }
    }

    // ── Legacy helpers kept for PlannerViewModel compatibility ──────────

    suspend fun createTask(
        title: String,
        description: String?,
        dueDate: LocalDate?
    ): AppResult<Task> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not authenticated"))
        val now = Instant.now()
        val task = Task(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.PENDING,
            dueAt = dueDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC),
            recurrenceRule = null,
            categoryId = null,
            linkedEntityId = null,
            linkedEntityType = null,
            isArchived = false,
            createdAt = now,
            updatedAt = now
        )
        return createTask(task)
    }

    suspend fun getTasksForDate(date: LocalDate): List<TaskEntity> {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return taskDao.getTasksForDateRange(startOfDay, endOfDay)
    }
}
