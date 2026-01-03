package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.TaskEntity
import com.ninety5.habitate.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE status != 'ARCHIVED' ORDER BY dueAt IS NULL, dueAt")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'OPEN' ORDER BY dueAt IS NULL, dueAt")
    suspend fun getIncompleteTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status = 'OPEN' AND dueAt < :now")
    suspend fun getOverdueTasks(now: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus, now: Long = System.currentTimeMillis())

    @Transaction
    suspend fun upsertAndMarkSynced(task: TaskEntity) {
        upsert(task.copy(syncState = SyncState.SYNCED))
    }

    @Query("UPDATE tasks SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)

    @Query("UPDATE tasks SET status = 'ARCHIVED', syncState = 'PENDING', updatedAt = :now WHERE status = 'DONE' AND updatedAt < :cutoff")
    suspend fun archiveOldTasks(cutoff: java.time.Instant, now: java.time.Instant = java.time.Instant.now())
}
