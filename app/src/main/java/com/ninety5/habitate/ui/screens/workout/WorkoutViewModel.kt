package com.ninety5.habitate.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val importStatus: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    val workouts: StateFlow<List<Workout>> = workoutRepository.observeAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun importHealthConnectWorkouts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, importStatus = "Importing...")
            when (val result = workoutRepository.syncFromHealthConnect()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        importStatus = "Imported ${result.data} workouts"
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.message,
                        importStatus = "Import failed"
                    )
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
    
    fun clearImportStatus() {
        _uiState.value = _uiState.value.copy(importStatus = null)
    }
}
