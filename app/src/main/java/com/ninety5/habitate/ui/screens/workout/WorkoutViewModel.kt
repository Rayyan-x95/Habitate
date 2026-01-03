package com.ninety5.habitate.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.repository.WorkoutRepository
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

    val workouts: StateFlow<List<WorkoutEntity>> = workoutRepository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshWorkouts()
    }

    fun refreshWorkouts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            workoutRepository.refreshWorkouts()
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun importHealthConnectWorkouts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, importStatus = "Importing...")
            workoutRepository.importHealthConnectWorkouts()
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        importStatus = "Imported $count workouts"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                        importStatus = "Import failed"
                    )
                }
        }
    }
    
    fun clearImportStatus() {
        _uiState.value = _uiState.value.copy(importStatus = null)
    }
}
