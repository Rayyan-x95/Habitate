package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus

@Dao
interface SyncQueueDao {
    /**
     * Gets pending operations for sync.
     * - PENDING: Ready for first or retry attempt
     * - FAILED with retryCount < MAX_RETRIES: Eligible for retry
     * - Orders by createdAt to ensure FIFO processing and prevent dependent operation failures
     */
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR (status = 'FAILED' AND retryCount < 5) ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>
    
    /**
     * Reset stale IN_PROGRESS operations that may have been orphaned if worker was killed.
     * Operations in IN_PROGRESS for more than 5 minutes are reset to PENDING.
     * Uses COALESCE to fallback to createdAt if lastAttemptAt is null.
     */
    @Query("UPDATE sync_queue SET status = 'PENDING' WHERE status = 'IN_PROGRESS' AND COALESCE(lastAttemptAt, createdAt) < :cutoffTime")
    suspend fun resetStaleOperations(cutoffTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: SyncOperationEntity)

    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus)
    
    @Query("UPDATE sync_queue SET retryCount = :retryCount, status = :status WHERE id = :id")
    suspend fun updateRetry(id: Long, retryCount: Int, status: SyncStatus)
}
