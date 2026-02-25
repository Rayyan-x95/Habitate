package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.health.HealthConnectAdapter
import com.ninety5.habitate.data.local.HabitateDatabase
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.repository.WorkoutRepository
import com.squareup.moshi.Moshi
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService,
    private val moshi: Moshi,
    private val healthConnectAdapter: HealthConnectAdapter,
    private val database: HabitateDatabase
) : WorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWorkout(workoutId: String): AppResult<Workout> {
        return try {
            val entity = workoutDao.getWorkoutById(workoutId)
                ?: return AppResult.Error(AppError.NotFound("Workout not found"))
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get workout: $workoutId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun createWorkout(workout: Workout): AppResult<Workout> {
        return try {
            val id = workout.id.ifBlank { UUID.randomUUID().toString() }
            val entity = workout.copy(id = id).toEntity(SyncState.PENDING)

            database.withTransaction {
                workoutDao.upsert(entity)
                val payload = moshi.adapter(WorkoutEntity::class.java).toJson(entity)
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "workout",
                        entityId = id,
                        operation = "CREATE",
                        payload = payload,
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }
            
            // Attempt immediate sync
            try {
                val payload = moshi.adapter(WorkoutEntity::class.java).toJson(entity)
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload)
                apiService.create("workouts", requestBody)
                workoutDao.updateSyncState(id, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("workout", id, "CREATE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for workout $id, will retry later")
            }
            
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to create workout")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun updateWorkout(workout: Workout): AppResult<Unit> {
        return try {
            val entity = workout.toEntity(SyncState.PENDING)
            workoutDao.upsert(entity)

            val payload = moshi.adapter(WorkoutEntity::class.java).toJson(entity)
            syncQueueDao.insert(
                SyncOperationEntity(
                    entityType = "workout",
                    entityId = workout.id,
                    operation = "UPDATE",
                    payload = payload,
                    status = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    lastAttemptAt = null
                )
            )
            
            // Attempt immediate sync
            try {
                val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload)
                apiService.update("workouts", workout.id, requestBody)
                workoutDao.updateSyncState(workout.id, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("workout", workout.id, "UPDATE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for workout ${workout.id}, will retry later")
            }
            
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update workout: ${workout.id}")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun deleteWorkout(workoutId: String): AppResult<Unit> {
        return archiveWorkout(workoutId)
    }

    override suspend fun archiveWorkout(workoutId: String): AppResult<Unit> {
        return try {
            database.withTransaction {
                val existing = workoutDao.getWorkoutById(workoutId)
                    ?: throw IllegalArgumentException("Workout not found: $workoutId")
                workoutDao.upsert(
                    existing.copy(isArchived = true, syncState = SyncState.PENDING, updatedAt = Instant.now())
                )
                syncQueueDao.insert(
                    SyncOperationEntity(
                        entityType = "workout",
                        entityId = workoutId,
                        operation = "DELETE",
                        payload = "{}",
                        status = SyncStatus.PENDING,
                        createdAt = Instant.now(),
                        lastAttemptAt = null
                    )
                )
            }
            
            // Attempt immediate sync
            try {
                apiService.delete("workouts", workoutId)
                workoutDao.updateSyncState(workoutId, SyncState.SYNCED)
                syncQueueDao.deleteByEntity("workout", workoutId, "DELETE")
            } catch (e: Exception) {
                Timber.w(e, "Immediate sync failed for workout $workoutId, will retry later")
            }
            
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to archive workout: $workoutId")
            AppResult.Error(AppError.from(e))
        }
    }

    override suspend fun syncFromHealthConnect(): AppResult<Int> {
        if (!healthConnectAdapter.isAvailable() || !healthConnectAdapter.hasPermissions()) {
            return AppResult.Error(
                AppError.Validation("Health Connect not available or permissions missing")
            )
        }

        return try {
            val lastImport = workoutDao.getLastImportTimestamp()
                ?: Instant.now().minusSeconds(30L * 24 * 60 * 60)
            val healthWorkouts = healthConnectAdapter.readWorkouts(since = lastImport)

            var importedCount = 0
            healthWorkouts.forEach { hw ->
                // Use transaction to atomically check-then-insert
                database.withTransaction {
                    if (workoutDao.findByExternalId(hw.externalId) == null) {
                        val entity = WorkoutEntity(
                            id = UUID.randomUUID().toString(),
                            source = com.ninety5.habitate.data.local.entity.WorkoutSource.HEALTH_CONNECT,
                            externalId = hw.externalId,
                            type = hw.type,
                            startTs = hw.startTs,
                            endTs = hw.endTs,
                            distanceMeters = hw.distanceMeters,
                            calories = hw.calories,
                            syncState = SyncState.PENDING,
                            updatedAt = Instant.now()
                        )
                        workoutDao.upsert(entity)

                        val payload = moshi.adapter(WorkoutEntity::class.java).toJson(entity)
                        syncQueueDao.insert(
                            SyncOperationEntity(
                                entityType = "workout",
                                entityId = entity.id,
                                operation = "CREATE",
                                payload = payload,
                                status = SyncStatus.PENDING,
                                createdAt = Instant.now(),
                                lastAttemptAt = null
                            )
                        )
                        importedCount++
                    }
                }
            }
            AppResult.Success(importedCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync from Health Connect")
            AppResult.Error(AppError.from(e))
        }
    }
}
