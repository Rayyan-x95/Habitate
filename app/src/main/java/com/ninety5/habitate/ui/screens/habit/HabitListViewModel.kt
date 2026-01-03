package com.ninety5.habitate.ui.screens.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.local.entity.HabitMood
import com.ninety5.habitate.data.local.relation.HabitWithStreak
import com.ninety5.habitate.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Habit List screen.
 * 
 * Features:
 * - Display all active habits with streaks
 * - Quick completion from list
 * - Filter by category
 * - Search habits
 * - Handle offline state
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<HabitCategory?>(null)
    val selectedCategory: StateFlow<HabitCategory?> = _selectedCategory.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            combine(
                habitRepository.getActiveHabitsWithStreaks(),
                _searchQuery,
                _selectedCategory
            ) { habits, query, category ->
                var filtered = habits

                // Filter by category
                if (category != null) {
                    filtered = filtered.filter { it.habit.category == category }
                }

                // Filter by search query
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.habit.title.contains(query, ignoreCase = true) ||
                        it.habit.description?.contains(query, ignoreCase = true) == true
                    }
                }

                filtered
            }
            .catch { e ->
                Timber.e(e, "Error loading habits")
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load habits",
                        isLoading = false
                    )
                }
            }
            .collect { habits ->
                _uiState.update {
                    it.copy(
                        habits = habits,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    /**
     * Check if habit is completed today.
     */
    fun isCompletedToday(habitId: String): Flow<Boolean> {
        return habitRepository.isCompletedToday(habitId)
    }

    /**
     * Quick complete a habit from the list.
     */
    fun completeHabit(habitId: String, mood: HabitMood? = null) {
        viewModelScope.launch {
            habitRepository.completeHabit(habitId, mood, null)
                .onSuccess {
                    _uiState.update { it.copy(lastCompletedHabitId = habitId) }
                    Timber.d("Habit completed: $habitId")
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
     * Archive a habit.
     */
    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
                .onSuccess {
                    Timber.d("Habit archived: $habitId")
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to archive habit")
                    }
                    Timber.e(e, "Failed to archive habit")
                }
        }
    }

    /**
     * Update search query.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Select a category filter.
     */
    fun onCategorySelected(category: HabitCategory?) {
        _selectedCategory.value = category
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Retry loading habits.
     */
    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadHabits()
    }
}

/**
 * UI state for Habit List screen.
 */
data class HabitListUiState(
    val habits: List<HabitWithStreak> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastCompletedHabitId: String? = null
)
