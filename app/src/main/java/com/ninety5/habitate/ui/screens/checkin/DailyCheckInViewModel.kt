package com.ninety5.habitate.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.DailySummary
import com.ninety5.habitate.domain.repository.DailyCheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyCheckInViewModel @Inject constructor(
    private val dailyCheckInRepository: DailyCheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        loadTodaySummary()
    }

    private fun loadTodaySummary() {
        viewModelScope.launch {
            dailyCheckInRepository.observeTodaySummary().collect { summary ->
                if (summary != null) {
                    _uiState.update {
                        it.copy(
                            mood = summary.mood,
                            notes = summary.notes,
                            existingSummary = summary
                        )
                    }
                }
            }
        }
    }

    fun setMood(mood: String) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveCheckIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            when (val result = dailyCheckInRepository.saveCheckIn(_uiState.value.mood, _uiState.value.notes)) {
                is AppResult.Success -> _uiState.update { it.copy(isSaved = true) }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}

data class CheckInUiState(
    val mood: String? = null,
    val notes: String? = null,
    val existingSummary: DailySummary? = null,
    val isSaved: Boolean = false,
    val error: String? = null
)
