package com.ninety5.habitate.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.repository.TaskRepository
import com.ninety5.habitate.domain.ai.AICoachingService
import com.ninety5.habitate.domain.model.Habit
import com.ninety5.habitate.domain.model.HabitCategory
import com.ninety5.habitate.domain.model.HabitFrequency
import com.ninety5.habitate.domain.repository.HabitRepository
import com.ninety5.habitate.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val aiCoachingService: AICoachingService,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlannerUiState>(PlannerUiState.Loading)
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    private val _events = Channel<PlannerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Cache for offline support
    private var cachedAdvice: String? = null
    private val _dismissedSuggestionIds = MutableStateFlow<Set<String>>(emptySet())
    
    // Expose selected date for UI
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadPlan()
    }

    fun loadPlan() {
        viewModelScope.launch {
            _uiState.value = PlannerUiState.Loading
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = PlannerUiState.Error("User not logged in")
                    return@launch
                }
                
                // Use selected date for loading tasks/habits
                val selectedDate = _selectedDate.value

                // Load all data in parallel
                val dailyAdvice = try {
                    val advice = aiCoachingService.getDailyAdvice(currentUser.id)
                    cachedAdvice = advice
                    advice
                } catch (e: Exception) {
                    // Use cached advice if available, otherwise use fallback
                    cachedAdvice ?: getFallbackAdvice()
                }

                val todayTasks = loadTasksForDate(selectedDate)
                val todayHabits = loadTodayHabits()
                val weeklyPlan = generateWeeklyPlan(selectedDate)
                val suggestions = generateSuggestions(currentUser.id)
                    .filter { it.id !in _dismissedSuggestionIds.value }

                _uiState.value = PlannerUiState.Success(
                    dailyAdvice = dailyAdvice,
                    todayTasks = todayTasks,
                    todayHabits = todayHabits,
                    weeklyPlan = weeklyPlan,
                    suggestions = suggestions
                )
            } catch (e: Exception) {
                // Check if we have cached data for offline mode
                if (cachedAdvice != null) {
                    _uiState.value = PlannerUiState.Offline(cachedAdvice)
                } else {
                    _uiState.value = PlannerUiState.Error("Failed to load plan: ${e.message}")
                }
            }
        }
    }

    fun refresh() {
        loadPlan()
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId)
                // Update UI state
                val currentState = _uiState.value
                if (currentState is PlannerUiState.Success) {
                    val updatedTasks = currentState.todayTasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = true) else task
                    }
                    _uiState.value = currentState.copy(todayTasks = updatedTasks)
                }
            } catch (e: Exception) {
                _events.send(PlannerEvent.Error("Failed to complete task"))
            }
        }
    }

    fun selectDay(date: LocalDate) {
        _selectedDate.value = date
        viewModelScope.launch {
            // Reload plan with selected date context
            loadPlan()
        }
    }

    fun acceptSuggestion(suggestion: AISuggestion) {
        viewModelScope.launch {
            try {
                when (suggestion.type) {
                    SuggestionType.NEW_HABIT -> {
                        // Create new habit from suggestion
                        val now = java.time.Instant.now()
                        val habit = Habit(
                            id = java.util.UUID.randomUUID().toString(),
                            userId = "", // Repository fills this from SecurePreferences
                            title = suggestion.title,
                            description = suggestion.description,
                            category = HabitCategory.CUSTOM,
                            color = "#6366F1",
                            icon = suggestion.emoji,
                            frequency = HabitFrequency.DAILY,
                            customSchedule = emptyList(),
                            reminderTime = null,
                            isArchived = false,
                            createdAt = now,
                            updatedAt = now
                        )
                        habitRepository.createHabit(habit)
                    }
                    SuggestionType.NEW_TASK -> {
                        // Create new task from suggestion
                        val now = java.time.Instant.now()
                        val dueDate = LocalDate.now().plusDays(1)
                        val task = com.ninety5.habitate.domain.model.Task(
                            id = java.util.UUID.randomUUID().toString(),
                            userId = "", // Repository fills from SecurePreferences
                            title = suggestion.title,
                            description = suggestion.description,
                            priority = com.ninety5.habitate.domain.model.TaskPriority.MEDIUM,
                            status = com.ninety5.habitate.domain.model.TaskStatus.PENDING,
                            dueAt = dueDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                            recurrenceRule = null,
                            categoryId = null,
                            linkedEntityId = null,
                            linkedEntityType = null,
                            isArchived = false,
                            createdAt = now,
                            updatedAt = now
                        )
                        taskRepository.createTask(task)
                    }
                    SuggestionType.SCHEDULE_CHANGE -> {
                        // Just mark as accepted - user acknowledges the advice
                    }
                    SuggestionType.WELLNESS_TIP -> {
                        // Just mark as accepted
                    }
                }

                // Remove from suggestions
                _dismissedSuggestionIds.update { it + suggestion.id }
                updateSuggestionsInState()
                
                _events.send(PlannerEvent.TaskAccepted)
            } catch (e: Exception) {
                _events.send(PlannerEvent.Error("Failed to accept suggestion"))
            }
        }
    }

    fun dismissSuggestion(suggestion: AISuggestion) {
        _dismissedSuggestionIds.update { it + suggestion.id }
        updateSuggestionsInState()
        viewModelScope.launch {
            _events.send(PlannerEvent.TaskDismissed)
        }
    }

    private fun updateSuggestionsInState() {
        val currentState = _uiState.value
        if (currentState is PlannerUiState.Success) {
            _uiState.value = currentState.copy(
                suggestions = currentState.suggestions.filter { it.id !in _dismissedSuggestionIds.value }
            )
        }
    }

    private suspend fun loadTasksForDate(date: LocalDate): List<PlannedTask> {
        return try {
            val allTasks = taskRepository.observeAllTasks().firstOrNull() ?: emptyList()
            val zone = java.time.ZoneId.systemDefault()
            val startOfDay = date.atStartOfDay(zone).toInstant()
            val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant()
            allTasks.filter { task ->
                task.dueAt != null && !task.dueAt.isBefore(startOfDay) && task.dueAt.isBefore(endOfDay)
            }.map { task ->
                PlannedTask(
                    id = task.id,
                    title = task.title,
                    isCompleted = task.status == com.ninety5.habitate.domain.model.TaskStatus.COMPLETED,
                    scheduledTime = task.dueAt?.let { 
                        java.time.LocalDateTime.ofInstant(it, zone)
                            .toLocalTime().toString() 
                    },
                    priority = when (task.priority) {
                        com.ninety5.habitate.domain.model.TaskPriority.HIGH,
                        com.ninety5.habitate.domain.model.TaskPriority.URGENT -> TaskPriority.HIGH
                        com.ninety5.habitate.domain.model.TaskPriority.MEDIUM -> TaskPriority.MEDIUM
                        com.ninety5.habitate.domain.model.TaskPriority.LOW -> TaskPriority.LOW
                    }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun loadTodayHabits(): List<PlannedHabit> {
        return try {
            val detailsList = habitRepository.observeActiveHabitsWithStreaks().firstOrNull() ?: emptyList()
            detailsList.map { hwd ->
                val today = LocalDate.now()
                val completedToday = hwd.recentLogs.any { log ->
                    log.completedAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today
                }
                PlannedHabit(
                    id = hwd.habit.id,
                    name = hwd.habit.title,
                    emoji = hwd.habit.icon,
                    streak = hwd.streak.currentStreak,
                    isCompletedToday = completedToday
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateWeeklyPlan(selectedDate: LocalDate): List<DayPlan> {
        // Start week from the beginning of the week containing selectedDate
        val startOfWeek = selectedDate.with(java.time.DayOfWeek.MONDAY)
        return (0..6).map { dayOffset ->
            val date = startOfWeek.plusDays(dayOffset.toLong())
            // Use deterministic values based on day of week
            val dayOfWeek = date.dayOfWeek.value
            DayPlan(
                date = date,
                taskCount = 2 + (dayOfWeek % 3), // 2-4 based on day
                habitCount = 2 + (dayOfWeek % 2), // 2-3 based on day
                completionRate = if (dayOffset == 0) 0.3f else 0f,
                focusArea = when (date.dayOfWeek.value) {
                    1 -> "Productivity"
                    2 -> "Learning"
                    3 -> "Wellness"
                    4 -> "Social"
                    5 -> "Creative"
                    else -> null
                }
            )
        }
    }

    private suspend fun generateSuggestions(userId: String): List<AISuggestion> {
        // Generate suggestions based on user patterns
        val suggestions = mutableListOf<AISuggestion>()

        // Check habit completion rate
        try {
            val habitList = habitRepository.observeAllHabits().firstOrNull() ?: emptyList()
            
            // Suggest new habits based on missing categories
            val categories = habitList.map { it.category }.toSet()
            if (HabitCategory.FITNESS !in categories) {
                suggestions.add(
                    AISuggestion(
                        id = "suggest_fitness",
                        type = SuggestionType.NEW_HABIT,
                        title = "Morning Stretch",
                        description = "Start your day with a 5-minute stretch routine",
                        emoji = "🧘",
                        reason = "You don't have any fitness habits yet"
                    )
                )
            }
            
            if (HabitCategory.MINDFULNESS !in categories) {
                suggestions.add(
                    AISuggestion(
                        id = "suggest_mindfulness",
                        type = SuggestionType.NEW_HABIT,
                        title = "Daily Gratitude",
                        description = "Write 3 things you're grateful for each morning",
                        emoji = "🙏",
                        reason = "Mindfulness habits boost wellbeing"
                    )
                )
            }
        } catch (e: Exception) {
            // Add fallback suggestions
            suggestions.addAll(getFallbackSuggestions())
        }

        // Time-based suggestions
        val hour = java.time.LocalTime.now().hour
        if (hour >= 20) {
            suggestions.add(
                AISuggestion(
                    id = "evening_wind_down",
                    type = SuggestionType.WELLNESS_TIP,
                    title = "Wind Down Time",
                    description = "Consider putting your phone away 30 minutes before bed for better sleep",
                    emoji = "🌙",
                    reason = "It's evening - good sleep habits matter"
                )
            )
        }

        return suggestions.take(5) // Limit to 5 suggestions
    }

    private fun getFallbackSuggestions(): List<AISuggestion> = listOf(
        AISuggestion(
            id = "fallback_hydration",
            type = SuggestionType.NEW_HABIT,
            title = "Stay Hydrated",
            description = "Drink 8 glasses of water throughout the day",
            emoji = "💧",
            reason = "Hydration improves focus and energy"
        ),
        AISuggestion(
            id = "fallback_reading",
            type = SuggestionType.NEW_HABIT,
            title = "Read for 15 Minutes",
            description = "Read something educational or inspiring daily",
            emoji = "📚",
            reason = "Reading expands your perspective"
        )
    )

    private fun getFallbackAdvice(): String {
        val tips = listOf(
            "Focus on completing your most important task first thing in the morning.",
            "Take short breaks every 90 minutes to maintain peak productivity.",
            "Review your habits before bed to set intentions for tomorrow.",
            "Small consistent actions lead to remarkable results over time.",
            "Celebrate small wins - they build momentum for bigger achievements."
        )
        return tips.random()
    }
}

// UI State
sealed class PlannerUiState {
    object Loading : PlannerUiState()
    data class Success(
        val dailyAdvice: String,
        val todayTasks: List<PlannedTask> = emptyList(),
        val todayHabits: List<PlannedHabit> = emptyList(),
        val weeklyPlan: List<DayPlan> = emptyList(),
        val suggestions: List<AISuggestion> = emptyList()
    ) : PlannerUiState()
    data class Error(val message: String) : PlannerUiState()
    data class Offline(val cachedAdvice: String?) : PlannerUiState()
}

// Events
sealed class PlannerEvent {
    object TaskAccepted : PlannerEvent()
    object TaskDismissed : PlannerEvent()
    data class Error(val message: String) : PlannerEvent()
}

// Data classes for planner
data class PlannedTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val scheduledTime: String? = null,
    val priority: TaskPriority? = null
)

enum class TaskPriority { HIGH, MEDIUM, LOW }

data class PlannedHabit(
    val id: String,
    val name: String,
    val emoji: String,
    val streak: Int,
    val isCompletedToday: Boolean
)

data class DayPlan(
    val date: LocalDate,
    val taskCount: Int,
    val habitCount: Int,
    val completionRate: Float,
    val focusArea: String? = null
)

data class AISuggestion(
    val id: String,
    val type: SuggestionType,
    val title: String,
    val description: String,
    val emoji: String,
    val reason: String? = null
)

enum class SuggestionType {
    NEW_HABIT,
    NEW_TASK,
    SCHEDULE_CHANGE,
    WELLNESS_TIP
}
