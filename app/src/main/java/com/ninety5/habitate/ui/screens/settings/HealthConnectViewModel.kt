package com.ninety5.habitate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.health.HealthConnectAdapter
import com.ninety5.habitate.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HealthUiState {
    object Loading : HealthUiState()
    object NotAvailable : HealthUiState()
    object NeedsPermission : HealthUiState()
    object Ready : HealthUiState()
}

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectAdapter: HealthConnectAdapter,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch {
            if (!healthConnectAdapter.isAvailable()) {
                _uiState.value = HealthUiState.NotAvailable
            } else if (!healthConnectAdapter.hasPermissions()) {
                _uiState.value = HealthUiState.NeedsPermission
            } else {
                _uiState.value = HealthUiState.Ready
            }
        }
    }
    
    fun getPermissions() = healthConnectAdapter.getRequiredPermissions()

    fun importWorkouts() {
        viewModelScope.launch {
            _importStatus.value = "Importing..."
            when (val result = workoutRepository.syncFromHealthConnect()) {
                is AppResult.Success -> _importStatus.value = "Imported ${result.data} workouts"
                is AppResult.Error -> _importStatus.value = "Error: ${result.error.message}"
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
