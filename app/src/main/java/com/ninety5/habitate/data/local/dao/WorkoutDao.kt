package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE externalId = :externalId LIMIT 1")
    suspend fun findByExternalId(externalId: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: String): WorkoutEntity?

    @Query("SELECT * FROM workouts ORDER BY startTs DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(workout: WorkoutEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: WorkoutEntity)

    @Query("SELECT MAX(endTs) FROM workouts")
    suspend fun getLastImportTimestamp(): java.time.Instant?

    @Query("UPDATE workouts SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: com.ninety5.habitate.data.local.entity.SyncState)

    @Query("UPDATE workouts SET isArchived = 1, syncState = 'PENDING', updatedAt = :now WHERE startTs < :cutoff")
    suspend fun archiveOldWorkouts(cutoff: java.time.Instant, now: java.time.Instant = java.time.Instant.now())

    @Query("UPDATE workouts SET isArchived = 0, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun restoreWorkout(id: String, now: java.time.Instant = java.time.Instant.now())
}
