package com.ninety5.habitate.ui.screens.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitMood
import com.ninety5.habitate.data.local.entity.HabitStreakEntity
import com.ninety5.habitate.data.local.relation.HabitWithLogs
import com.ninety5.habitate.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
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
        loadStreak()
    }

    /**
     * Retry loading habit data after an error.
     */
    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadHabitDetails()
        loadStreak()
    }

    private fun loadHabitDetails() {
        viewModelScope.launch {
            habitRepository.getHabitWithLogs(habitId)
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
                .collect { habitWithLogs ->
                    _uiState.update {
                        it.copy(
                            habitWithLogs = habitWithLogs,
                            heatmapData = generateHeatmapData(habitWithLogs.logs),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadStreak() {
        viewModelScope.launch {
            habitRepository.getStreak(habitId)
                .catch { e ->
                    Timber.e(e, "Error loading streak")
                }
                .collect { streak ->
                    _uiState.update { it.copy(streak = streak) }
                }
        }
    }

    /**
     * Generate heatmap data for last 365 days.
     */
    private fun generateHeatmapData(logs: List<HabitLogEntity>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val startDate = today.minusDays(364)
        
        return logs
            .filter { log ->
                val logDate = java.time.Instant.ofEpochMilli(log.completedAt.toEpochMilli())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                !logDate.isBefore(startDate)
            }
            .groupBy { log ->
                java.time.Instant.ofEpochMilli(log.completedAt.toEpochMilli())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { it.value.size }
    }

    /**
     * Complete habit with optional mood and note.
     */
    fun completeHabit(mood: HabitMood?, note: String?) {
        viewModelScope.launch {
            habitRepository.completeHabit(habitId, mood, note)
                .onSuccess {
                    Timber.d("Habit completed: $habitId")
                    _uiState.update { it.copy(showCompletionSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to complete habit")
                    }
                    Timber.e(e, "Failed to complete habit")
                }
        }
    }

    /**
     * Undo completion for specific date.
     */
    fun uncompleteHabit(date: String) {
        viewModelScope.launch {
            habitRepository.uncompleteHabit(habitId, date)
                .onSuccess {
                    Timber.d("Habit uncompleted: $habitId")
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to undo completion")
                    }
                    Timber.e(e, "Failed to undo completion")
                }
        }
    }

    /**
     * Delete habit permanently.
     */
    fun deleteHabit() {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
                .onSuccess {
                    Timber.d("Habit deleted: $habitId")
                    _uiState.update { it.copy(habitDeleted = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to delete habit")
                    }
                    Timber.e(e, "Failed to delete habit")
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
    val habitWithLogs: HabitWithLogs? = null,
    val streak: HabitStreakEntity? = null,
    val heatmapData: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCompletionSuccess: Boolean = false,
    val habitDeleted: Boolean = false
)
