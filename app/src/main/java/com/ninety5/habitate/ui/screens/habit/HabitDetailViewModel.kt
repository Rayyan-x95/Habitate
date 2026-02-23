package com.ninety5.habitate.ui.screens.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.HabitLog
import com.ninety5.habitate.domain.model.HabitMood
import com.ninety5.habitate.domain.model.HabitStreak
import com.ninety5.habitate.domain.model.HabitWithDetails
import com.ninety5.habitate.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel for Habit Detail screen.
 * 
 * Features:
 * - View habit details and history
 * - Calendar heatmap data
 * - Mood trends
 * - Streak information
 * - Edit/Delete actions
 */
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        loadHabitDetails()
    }

    /**
     * Retry loading habit data after an error.
     */
    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadHabitDetails()
    }

    private fun loadHabitDetails() {
        viewModelScope.launch {
            habitRepository.observeHabitWithDetails(habitId)
                .catch { e ->
                    Timber.e(e, "Error loading habit details")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to load habit",
                            isLoading = false
                        )
                    }
                }
                .filterNotNull()
                .collect { habitWithDetails ->
                    _uiState.update {
                        it.copy(
                            habitWithDetails = habitWithDetails,
                            streak = habitWithDetails.streak,
                            heatmapData = generateHeatmapData(habitWithDetails.recentLogs),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Generate heatmap data for last 365 days.
     */
    private fun generateHeatmapData(logs: List<HabitLog>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val startDate = today.minusDays(364)
        
        return logs
            .filter { log ->
                val logDate = log.completedAt
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                !logDate.isBefore(startDate)
            }
            .groupBy { log ->
                log.completedAt
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { it.value.size }
    }

    /**
     * Complete habit with optional mood and note.
     */
    fun completeHabit(mood: HabitMood?, note: String?) {
        viewModelScope.launch {
            when (val result = habitRepository.logCompletion(habitId, mood, note)) {
                is AppResult.Success -> {
                    Timber.d("Habit completed: $habitId")
                    _uiState.update { it.copy(showCompletionSuccess = true) }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.error.message)
                    }
                    Timber.e("Failed to complete habit: ${result.error.message}")
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Undo completion for specific date.
     */
    fun uncompleteHabit(date: String) {
        viewModelScope.launch {
            when (val result = habitRepository.undoCompletion(habitId, date)) {
                is AppResult.Success -> Timber.d("Habit uncompleted: $habitId")
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.error.message)
                    }
                    Timber.e("Failed to undo completion: ${result.error.message}")
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Delete habit permanently.
     */
    fun deleteHabit() {
        viewModelScope.launch {
            when (val result = habitRepository.deleteHabit(habitId)) {
                is AppResult.Success -> {
                    Timber.d("Habit deleted: $habitId")
                    _uiState.update { it.copy(habitDeleted = true) }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.error.message)
                    }
                    Timber.e("Failed to delete habit: ${result.error.message}")
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Clear success/error states.
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(
                error = null,
                showCompletionSuccess = false
            )
        }
    }
}

/**
 * UI state for Habit Detail screen.
 */
data class HabitDetailUiState(
    val habitWithDetails: HabitWithDetails? = null,
    val streak: HabitStreak? = null,
    val heatmapData: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCompletionSuccess: Boolean = false,
    val habitDeleted: Boolean = false
)
