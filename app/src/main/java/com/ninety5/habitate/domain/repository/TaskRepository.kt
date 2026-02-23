package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for task operations.
 */
interface TaskRepository {
    fun observeAllTasks(): Flow<List<Task>>
    fun observeTasksByStatus(status: String): Flow<List<Task>>
    suspend fun getTask(taskId: String): AppResult<Task>
    suspend fun createTask(task: Task): AppResult<Task>
    suspend fun updateTask(task: Task): AppResult<Unit>
    suspend fun deleteTask(taskId: String): AppResult<Unit>
    suspend fun completeTask(taskId: String): AppResult<Unit>
    suspend fun archiveTask(taskId: String): AppResult<Unit>
}
