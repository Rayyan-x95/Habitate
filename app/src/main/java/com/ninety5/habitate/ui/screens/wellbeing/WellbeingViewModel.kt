package com.ninety5.habitate.ui.screens.wellbeing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.domain.model.Habit
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.domain.model.JournalMood
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.repository.HabitRepository
import com.ninety5.habitate.domain.repository.JournalRepository
import com.ninety5.habitate.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class WellbeingViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WellbeingUiState())
    val uiState: StateFlow<WellbeingUiState> = _uiState.asStateFlow()
    
    private var wellbeingJob: Job? = null

    init {
        loadWellbeingData()
    }

    private fun loadWellbeingData() {
        wellbeingJob?.cancel()
        wellbeingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    journalRepository.observeAllEntries(),
                    habitRepository.observeAllHabits(),
                    workoutRepository.observeAllWorkouts()
                ) { journalEntries, habits, workouts ->
                    Triple(journalEntries, habits, workouts)
                }.collect { (journalEntries, habits, workouts) ->
                    
                    val moodData = analyzeMoodTrends(journalEntries)
                    val habitCompletionRate = calculateHabitCompletionRate(habits)
                    val workoutStats = analyzeWorkouts(workouts)
                    val wellbeingScore = calculateWellbeingScore(moodData, habitCompletionRate, workoutStats)
                    val insights = generateInsights(moodData, habitCompletionRate, workoutStats)
                    
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            wellbeingScore = wellbeingScore,
                            moodData = moodData,
                            habitCompletionRate = habitCompletionRate,
                            workoutStats = workoutStats,
                            insights = insights,
                            recentJournalEntries = journalEntries.take(5),
                            totalHabits = habits.size,
                            activeHabits = habits.count { !it.isArchived },
                            totalWorkouts = workouts.size,
                            workoutsThisWeek = workouts.count { workout ->
                                val workoutDate = workout.startTime
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                val cutoff = LocalDate.now().minusWeeks(1)
                                !workoutDate.isBefore(cutoff)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun analyzeMoodTrends(entries: List<JournalEntry>): MoodData {
        val cutoffDate = LocalDate.now().minusDays(7)
        val last7Days = entries.filter { entry ->
            val entryDate = entry.createdAt
                .atZone(ZoneId.systemDefault()).toLocalDate()
            !entryDate.isBefore(cutoffDate)
        }
        
        val moodCounts = last7Days.groupBy { it.mood?.name?.lowercase() ?: "unknown" }
            .mapValues { it.value.size }
        
        val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: "neutral"
        
        val positiveCount = last7Days.count { entry ->
            entry.mood in listOf(JournalMood.AMAZING, JournalMood.HAPPY)
        }
        val totalWithMood = last7Days.count { it.mood != null }
        val positivityRate = if (totalWithMood > 0) positiveCount.toFloat() / totalWithMood else 0.5f
        
        return MoodData(
            dominantMood = dominantMood,
            positivityRate = positivityRate,
            entriesThisWeek = last7Days.size,
            moodDistribution = moodCounts
        )
    }

    private fun calculateHabitCompletionRate(habits: List<Habit>): Float {
        if (habits.isEmpty()) return 0f
        
        // Filter out archived habits
        val activeHabits = habits.filter { !it.isArchived }
        if (activeHabits.isEmpty()) return 0f
        
        // Note: Actual completion tracking would require checking HabitLogEntity
        // For now, return a placeholder rate
        return 0.5f
    }

    private fun analyzeWorkouts(workouts: List<Workout>): WorkoutStats {
        val cutoffDate = LocalDate.now().minusWeeks(1)
        val thisWeek = workouts.filter { workout ->
            val workoutDate = workout.startTime
                .atZone(ZoneId.systemDefault()).toLocalDate()
            !workoutDate.isBefore(cutoffDate) // Include boundary date
        }
        
        val totalDuration = thisWeek.sumOf { (it.durationSeconds + 30) / 60 } // Round to nearest minute
        
        val totalCalories = thisWeek.sumOf { it.caloriesBurned ?: 0 }
        
        // Calculate normalized intensity (calories per minute)
        val averageIntensity = if (thisWeek.isNotEmpty() && totalDuration > 0) {
            totalCalories.toFloat() / totalDuration.toFloat()
        } else 0f
        
        return WorkoutStats(
            workoutsThisWeek = thisWeek.size,
            totalMinutes = totalDuration.toInt(),
            totalCalories = totalCalories,
            averageIntensity = averageIntensity
        )
    }

    private fun calculateWellbeingScore(
        moodData: MoodData,
        habitCompletionRate: Float,
        workoutStats: WorkoutStats
    ): Int {
        // Weighted scoring (values already in 0-1 range, multiply by weight)
        val moodScore = moodData.positivityRate * 35 // 35% weight
        val habitScore = habitCompletionRate * 35 // 35% weight
        val activityScore = minOf(workoutStats.workoutsThisWeek.toFloat() / 4, 1f) * 30 // 30% weight
        
        return (moodScore + habitScore + activityScore).toInt().coerceIn(0, 100)
    }

    private fun generateInsights(
        moodData: MoodData,
        habitCompletionRate: Float,
        workoutStats: WorkoutStats
    ): List<WellbeingInsight> {
        val insights = mutableListOf<WellbeingInsight>()
        
        // Mood insights
        when {
            moodData.positivityRate >= 0.7f -> {
                insights.add(WellbeingInsight(
                    emoji = "🌟",
                    title = "Great mood this week!",
                    description = "Your journal entries show positive emotions. Keep it up!"
                ))
            }
            moodData.positivityRate < 0.3f -> {
                insights.add(WellbeingInsight(
                    emoji = "💙",
                    title = "Tough week?",
                    description = "Consider reaching out to friends or trying some self-care activities."
                ))
            }
            moodData.entriesThisWeek == 0 -> {
                insights.add(WellbeingInsight(
                    emoji = "📝",
                    title = "Start journaling",
                    description = "Writing down your thoughts can improve mental clarity."
                ))
            }
        }
        
        // Habit insights
        when {
            habitCompletionRate >= 0.8f -> {
                insights.add(WellbeingInsight(
                    emoji = "🏆",
                    title = "Habit champion!",
                    description = "You're crushing your habits today. Amazing consistency!"
                ))
            }
            habitCompletionRate < 0.3f -> {
                insights.add(WellbeingInsight(
                    emoji = "🎯",
                    title = "Room to grow",
                    description = "Try focusing on just 1-2 key habits to build momentum."
                ))
            }
        }
        
        // Workout insights
        when {
            workoutStats.workoutsThisWeek >= 4 -> {
                insights.add(WellbeingInsight(
                    emoji = "💪",
                    title = "Active lifestyle!",
                    description = "${workoutStats.workoutsThisWeek} workouts this week. Your body thanks you!"
                ))
            }
            workoutStats.workoutsThisWeek == 0 -> {
                insights.add(WellbeingInsight(
                    emoji = "🚶",
                    title = "Time to move",
                    description = "Even a short walk can boost your mood and energy."
                ))
            }
        }
        
        return insights.take(4)
    }

    fun refresh() {
        loadWellbeingData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class WellbeingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val wellbeingScore: Int = 0,
    val moodData: MoodData = MoodData(),
    val habitCompletionRate: Float = 0f,
    val workoutStats: WorkoutStats = WorkoutStats(),
    val insights: List<WellbeingInsight> = emptyList(),
    val recentJournalEntries: List<JournalEntry> = emptyList(),
    val totalHabits: Int = 0,
    val activeHabits: Int = 0,
    val totalWorkouts: Int = 0,
    val workoutsThisWeek: Int = 0
)

data class MoodData(
    val dominantMood: String = "neutral",
    val positivityRate: Float = 0.5f,
    val entriesThisWeek: Int = 0,
    val moodDistribution: Map<String, Int> = emptyMap()
)

data class WorkoutStats(
    val workoutsThisWeek: Int = 0,
    val totalMinutes: Int = 0,
    val totalCalories: Int = 0,
    val averageIntensity: Float = 0f
)

data class WellbeingInsight(
    val emoji: String,
    val title: String,
    val description: String
)
