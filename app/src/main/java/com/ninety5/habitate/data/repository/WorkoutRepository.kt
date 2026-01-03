package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.health.HealthConnectAdapter
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.local.entity.WorkoutSource
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi,
    private val healthConnectAdapter: HealthConnectAdapter
) {

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> {
        return workoutDao.getAllWorkouts()
    }

    suspend fun saveWorkout(workout: WorkoutEntity) {
        workoutDao.upsert(workout.copy(syncState = SyncState.PENDING))
        
        val payload = moshi.adapter(WorkoutEntity::class.java).toJson(workout)
        val syncOp = SyncOperationEntity(
            entityType = "workout",
            entityId = workout.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }
    
    suspend fun importHealthConnectWorkouts(): Result<Int> {
        if (!healthConnectAdapter.isAvailable() || !healthConnectAdapter.hasPermissions()) {
            return Result.failure(IllegalStateException("Health Connect not available or permissions missing"))
        }

        return try {
            // Default to last 30 days if no previous import
            val lastImport = workoutDao.getLastImportTimestamp() ?: Instant.now().minusSeconds(30 * 24 * 60 * 60)
            val healthWorkouts = healthConnectAdapter.readWorkouts(since = lastImport)
            
            var importedCount = 0
            
            healthWorkouts.forEach { hw ->
                // Deduplicate
                if (workoutDao.findByExternalId(hw.externalId) == null) {
                    val entity = WorkoutEntity(
                        id = UUID.randomUUID().toString(),
                        source = WorkoutSource.HEALTH_CONNECT,
                        externalId = hw.externalId,
                        type = hw.type,
                        startTs = hw.startTs,
                        endTs = hw.endTs,
                        distanceMeters = hw.distanceMeters,
                        calories = hw.calories,
                        syncState = SyncState.PENDING,
                        updatedAt = Instant.now()
                    )
                    saveWorkout(entity) // Reuse save logic to trigger sync
                    importedCount++
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshWorkouts(): Result<Unit> {
        return try {
            val workoutDtos = apiService.getWorkouts()
            val workouts = workoutDtos.map { dto ->
                WorkoutEntity(
                    id = dto.id,
                    source = try {
                        WorkoutSource.valueOf(dto.source)
                    } catch (e: IllegalArgumentException) {
                        WorkoutSource.MANUAL
                    },
                    externalId = dto.externalId,
                    type = dto.type,
                    startTs = dto.startTs,
                    endTs = dto.endTs,
                    distanceMeters = dto.distanceMeters,
                    calories = dto.calories,
                    syncState = SyncState.SYNCED,
                    updatedAt = dto.updatedAt
                )
            }
            workouts.forEach { workout ->
                workoutDao.upsert(workout.copy(syncState = SyncState.SYNCED))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
