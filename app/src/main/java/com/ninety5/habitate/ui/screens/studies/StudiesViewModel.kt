package com.ninety5.habitate.ui.screens.studies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StudiesViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudiesUiState())
    val uiState: StateFlow<StudiesUiState> = _uiState.asStateFlow()
    
    private var studyJob: Job? = null

    init {
        loadStudyData()
    }

    private fun loadStudyData() {
        studyJob?.cancel()
        studyJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Load study sessions (habits with LEARNING category or study-related titles)
                habitRepository.getAllHabits().collect { habits ->
                    val studyHabits = habits.filter { habit ->
                        habit.category == HabitCategory.LEARNING ||
                        habit.title.lowercase().contains("study") ||
                        habit.title.lowercase().contains("learn") ||
                        habit.title.lowercase().contains("read")
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            studyHabits = studyHabits.map { habit ->
                                StudyHabit(
                                    id = habit.id,
                                    name = habit.title,
                                    description = habit.description,
                                    targetMinutes = 30, // Default target
                                    completedToday = false, // Would check HabitLogEntity
                                    streak = 0 // Would check HabitStreakEntity
                                )
                            },
                            currentStreak = 0,
                            todayMinutes = 0,
                            weeklyGoal = 420, // 7 hours per week default
                            weeklyProgress = 0f
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun startStudySession(habitId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(activeSessionId = habitId, sessionStartTime = System.currentTimeMillis()) }
        }
    }

    fun endStudySession() {
        viewModelScope.launch {
            val state = _uiState.value
            val habitId = state.activeSessionId ?: return@launch
            val startTime = state.sessionStartTime ?: return@launch
            
            // Compute session duration
            val durationMs = System.currentTimeMillis() - startTime
            val durationMinutes = (durationMs / 1000 / 60).toInt()
            
            try {
                // Mark habit as completed for today
                habitRepository.completeHabit(habitId)
                
                // Update todayMinutes in UI state immediately
                _uiState.update { it.copy(
                    todayMinutes = it.todayMinutes + durationMinutes,
                    weeklyProgress = ((it.todayMinutes + durationMinutes).toFloat() / it.weeklyGoal).coerceIn(0f, 1f)
                )}
                
                Timber.d("Study session completed: $habitId, duration: $durationMinutes minutes")
            } catch (e: Exception) {
                Timber.e(e, "Failed to complete study session")
                _uiState.update { it.copy(error = e.message ?: "Failed to end session") }
            } finally {
                _uiState.update { it.copy(activeSessionId = null, sessionStartTime = null) }
                loadStudyData() // Refresh data
            }
        }
    }

    fun setStudyGoal(minutes: Int) {
        _uiState.update { it.copy(weeklyGoal = minutes * 7) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class StudiesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val studyHabits: List<StudyHabit> = emptyList(),
    val currentStreak: Int = 0,
    val todayMinutes: Int = 0,
    val weeklyGoal: Int = 420, // 7 hours
    val weeklyProgress: Float = 0f,
    val activeSessionId: String? = null,
    val sessionStartTime: Long? = null,
    val studyTips: List<String> = defaultStudyTips
)

data class StudyHabit(
    val id: String,
    val name: String,
    val description: String?,
    val targetMinutes: Int,
    val completedToday: Boolean,
    val streak: Int
)

private val defaultStudyTips = listOf(
    "🍅 Try the Pomodoro technique: 25 min focus + 5 min break",
    "📚 Review notes within 24 hours to boost retention by 80%",
    "🎯 Set specific goals for each study session",
    "💧 Stay hydrated - your brain needs water to function well",
    "😴 Sleep consolidates memory - aim for 7-8 hours",
    "🚶 Take a short walk between study sessions",
    "📝 Teaching others is the best way to learn",
    "🔄 Space out your learning for better long-term retention"
)
