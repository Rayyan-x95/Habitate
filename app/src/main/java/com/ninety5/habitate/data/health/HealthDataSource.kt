package com.ninety5.habitate.data.health

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface HealthDataSource {
    suspend fun isAvailable(): Boolean
    suspend fun hasPermissions(): Boolean
    suspend fun requestPermissions(activity: Activity)
    suspend fun readWorkouts(since: Instant): List<HealthWorkout>
    fun observeDailySteps(): Flow<Int>
}
