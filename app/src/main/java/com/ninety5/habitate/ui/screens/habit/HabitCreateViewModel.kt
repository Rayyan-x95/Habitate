package com.ninety5.habitate.ui.screens.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.local.entity.HabitFrequency
import com.ninety5.habitate.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for Habit Create/Edit screen.
 * 
 * Features:
 * - Form state management
 * - Validation
 * - Create/Update operations
 * - Category/color/icon selection
 */
@HiltViewModel
class HabitCreateViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: String? = savedStateHandle["habitId"]
    private val isEditMode: Boolean = habitId != null

    private val _uiState = MutableStateFlow(HabitCreateUiState())
    val uiState: StateFlow<HabitCreateUiState> = _uiState.asStateFlow()

    init {
        if (isEditMode && habitId != null) {
            loadHabitForEdit(habitId)
        }
    }

    private fun loadHabitForEdit(habitId: String) {
        viewModelScope.launch {
            habitRepository.getHabitWithLogs(habitId)
                .collect { habitWithLogs ->
                    habitWithLogs?.let { hwl ->
                        _uiState.update {
                            it.copy(
                                title = hwl.habit.title,
                                description = hwl.habit.description ?: "",
                                selectedCategory = hwl.habit.category,
                                selectedColor = hwl.habit.color,
                                selectedIcon = hwl.habit.icon,
                                selectedFrequency = hwl.habit.frequency,
                                customSchedule = hwl.habit.customSchedule ?: emptyList(),
                                reminderTime = hwl.habit.reminderTime,
                                reminderEnabled = hwl.habit.reminderEnabled,
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCategorySelected(category: HabitCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedColor = category.getColor() // Auto-set category color
            )
        }
    }

    fun onColorSelected(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun onIconSelected(icon: String) {
        _uiState.update { it.copy(selectedIcon = icon) }
    }

    fun onFrequencySelected(frequency: HabitFrequency) {
        _uiState.update {
            it.copy(
                selectedFrequency = frequency,
                customSchedule = if (frequency != HabitFrequency.CUSTOM) emptyList() else it.customSchedule
            )
        }
    }

    fun onCustomScheduleToggle(day: DayOfWeek) {
        _uiState.update {
            val schedule = it.customSchedule.toMutableList()
            if (day in schedule) {
                schedule.remove(day)
            } else {
                schedule.add(day)
            }
            it.copy(customSchedule = schedule)
        }
    }

    fun onReminderToggle(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time, reminderEnabled = true) }
    }

    fun clearReminder() {
        _uiState.update { it.copy(reminderTime = null, reminderEnabled = false) }
    }

    /**
     * Save habit (create or update).
     */
    fun saveHabit() {
        if (!validateForm()) return

        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = if (isEditMode && habitId != null) {
                habitRepository.updateHabit(
                    habitId = habitId,
                    title = state.title,
                    description = state.description.ifBlank { null },
                    category = state.selectedCategory,
                    color = state.selectedColor,
                    icon = state.selectedIcon,
                    frequency = state.selectedFrequency,
                    customSchedule = if (state.selectedFrequency == HabitFrequency.CUSTOM) state.customSchedule else null,
                    reminderTime = if (state.reminderEnabled) state.reminderTime else null,
                    reminderEnabled = state.reminderEnabled
                )
            } else {
                habitRepository.createHabit(
                    title = state.title,
                    description = state.description.ifBlank { null },
                    category = state.selectedCategory,
                    color = state.selectedColor,
                    icon = state.selectedIcon,
                    frequency = state.selectedFrequency,
                    customSchedule = if (state.selectedFrequency == HabitFrequency.CUSTOM) state.customSchedule else null,
                    reminderTime = if (state.reminderEnabled) state.reminderTime else null,
                    reminderEnabled = state.reminderEnabled
                ).map { Unit }
            }

            result
                .onSuccess {
                    Timber.d("Habit saved successfully")
                    _uiState.update { it.copy(isSaving = false, habitSaved = true) }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to save habit")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to save habit"
                        )
                    }
                }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            isValid = false
        }

        if (state.selectedFrequency == HabitFrequency.CUSTOM && state.customSchedule.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day") }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for Habit Create screen.
 */
data class HabitCreateUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: HabitCategory = HabitCategory.HEALTH,
    val selectedColor: String = HabitCategory.HEALTH.getColor(),
    val selectedIcon: String = "💪",
    val selectedFrequency: HabitFrequency = HabitFrequency.DAILY,
    val customSchedule: List<DayOfWeek> = emptyList(),
    val reminderTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val habitSaved: Boolean = false,
    val titleError: String? = null,
    val error: String? = null
)
