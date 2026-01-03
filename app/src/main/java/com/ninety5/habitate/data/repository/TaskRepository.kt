package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.TaskEntity
import com.ninety5.habitate.data.local.entity.TaskStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    fun getActiveTasks(): Flow<List<TaskEntity>> {
        return taskDao.getActiveTasks()
    }

    suspend fun getTaskById(taskId: String): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun createTask(task: TaskEntity) {
        taskDao.upsert(task.copy(syncState = SyncState.PENDING))
        
        val payload = moshi.adapter(TaskEntity::class.java).toJson(task)
        val syncOp = SyncOperationEntity(
            entityType = "task",
            entityId = task.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun updateStatus(taskId: String, status: TaskStatus) {
        taskDao.updateStatus(taskId, status)
        
        val payload = "{\"status\": \"$status\"}"
        val syncOp = SyncOperationEntity(
            entityType = "task",
            entityId = taskId,
            operation = "UPDATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun refreshTasks(): Result<Unit> {
        return try {
            val taskDtos = apiService.getTasks()
            val tasks = taskDtos.map { dto ->
                TaskEntity(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    dueAt = dto.dueAt,
                    recurrenceRule = dto.recurrenceRule,
                    status = try {
                        TaskStatus.valueOf(dto.status)
                    } catch (e: IllegalArgumentException) {
                        TaskStatus.OPEN // Fallback
                    },
                    syncState = SyncState.SYNCED,
                    updatedAt = dto.updatedAt
                )
            }
            tasks.forEach { taskDao.upsertAndMarkSynced(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
