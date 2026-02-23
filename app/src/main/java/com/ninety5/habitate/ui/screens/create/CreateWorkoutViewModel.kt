package com.ninety5.habitate.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.model.WorkoutSource
import com.ninety5.habitate.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class CreateWorkoutUiState(
    val type: String = "",
    val durationMinutes: String = "",
    val calories: String = "",
    val distanceMeters: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkoutUiState())
    val uiState: StateFlow<CreateWorkoutUiState> = _uiState.asStateFlow()

    fun onTypeChange(newValue: String) {
        _uiState.update { it.copy(type = newValue) }
    }

    fun onDurationChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(durationMinutes = newValue) }
        }
    }

    fun onCaloriesChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            _uiState.update { it.copy(calories = newValue) }
        }
    }

    fun onDistanceChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            _uiState.update { it.copy(distanceMeters = newValue) }
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _uiState.update { it.copy(startDate = newDate) }
    }

    fun onTimeChange(newTime: LocalTime) {
        _uiState.update { it.copy(startTime = newTime) }
    }

    fun createWorkout() {
        val currentState = _uiState.value
        if (currentState.type.isBlank() || currentState.durationMinutes.isBlank()) {
            _uiState.update { it.copy(error = "Type and Duration are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val startDateTime = currentState.startDate.atTime(currentState.startTime)
                val startTs = startDateTime.atZone(ZoneId.systemDefault()).toInstant()
                val duration = currentState.durationMinutes.toLongOrNull() ?: 0L
                val endTs = startTs.plus(duration, ChronoUnit.MINUTES)

                val workout = Workout(
                    id = UUID.randomUUID().toString(),
                    userId = "", // Repository fills from SecurePreferences
                    type = com.ninety5.habitate.domain.model.WorkoutType.valueOf(
                        currentState.type.uppercase().replace(" ", "_")
                            .let { if (com.ninety5.habitate.domain.model.WorkoutType.entries.any { e -> e.name == it }) it else "OTHER" }
                    ),
                    source = WorkoutSource.MANUAL,
                    externalId = null,
                    startTime = startTs,
                    endTime = endTs,
                    durationSeconds = duration * 60,
                    distanceMeters = currentState.distanceMeters.toDoubleOrNull(),
                    caloriesBurned = currentState.calories.toDoubleOrNull()?.toInt(),
                    heartRateAvg = null,
                    notes = null,
                    isArchived = false,
                    createdAt = Instant.now()
                )

                when (val result = workoutRepository.createWorkout(workout)) {
                    is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                    is AppResult.Loading -> { /* no-op */ }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
