package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Workout
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for workout operations.
 */
interface WorkoutRepository {
    fun observeAllWorkouts(): Flow<List<Workout>>
    suspend fun getWorkout(workoutId: String): AppResult<Workout>
    suspend fun createWorkout(workout: Workout): AppResult<Workout>
    suspend fun updateWorkout(workout: Workout): AppResult<Unit>
    suspend fun deleteWorkout(workoutId: String): AppResult<Unit>
    suspend fun archiveWorkout(workoutId: String): AppResult<Unit>
    suspend fun syncFromHealthConnect(): AppResult<Int>
}
