package com.ninety5.habitate.data.health

import android.app.Activity
import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class HealthConnectAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthDataSource {

    // Lazy initialization to avoid crash on devices without Health Connect
    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    private val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    override suspend fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    override suspend fun hasPermissions(): Boolean {
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun requestPermissions(activity: Activity) {
        // This method is intended to be called via ActivityResultContract in the UI layer.
        // We expose the permissions set via getRequiredPermissions() helper.
        throw UnsupportedOperationException("Use ActivityResultContract in UI with getRequiredPermissions()")
    }
    
    fun getRequiredPermissions() = permissions

    override suspend fun readWorkouts(since: Instant): List<HealthWorkout> {
        if (!isAvailable() || !hasPermissions()) return emptyList()

        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.after(since)
        )

        return try {
            val response = client.readRecords(request)
            response.records.map { session ->
                val calories = readCaloriesForSession(session.startTime, session.endTime)
                val distance = readDistanceForSession(session.startTime, session.endTime)

                HealthWorkout(
                    externalId = session.metadata.id,
                    type = getExerciseName(session.exerciseType),
                    startTs = session.startTime,
                    endTs = session.endTime,
                    calories = calories,
                    distanceMeters = distance
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read workouts")
            emptyList()
        }
    }

    private suspend fun readCaloriesForSession(start: Instant, end: Instant): Double? {
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun readDistanceForSession(start: Instant, end: Instant): Double? {
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response[DistanceRecord.DISTANCE_TOTAL]?.inMeters
        } catch (e: Exception) {
            null
        }
    }

    override fun observeDailySteps(): Flow<Int> = flow {
        // Polling implementation for steps (Health Connect doesn't support real-time flow yet)
        // In a real app, this would be triggered by a periodic worker or lifecycle event
        if (!isAvailable() || !hasPermissions()) {
            emit(0)
            return@flow
        }
        
        try {
            val now = Instant.now()
            val startOfDay = now // Simplified: In real app, calculate start of day
            
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay.minusSeconds(86400), now) // Last 24h for demo
                )
            )
            emit(response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0)
        } catch (e: Exception) {
            emit(0)
        }
    }

    private fun getExerciseName(type: Int): String {
        return when (type) {
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Cycling"
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Swimming"
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
            ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
            ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gymnastics"
            ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "Weightlifting"
            else -> "Workout"
        }
    }
}
