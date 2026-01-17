package com.ninety5.habitate.ui.screens.wellbeing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.repository.HabitRepository
import com.ninety5.habitate.data.repository.JournalRepository
import com.ninety5.habitate.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class WellbeingViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WellbeingUiState())
    val uiState: StateFlow<WellbeingUiState> = _uiState.asStateFlow()

    init {
        loadWellbeingData()
    }

    private fun loadWellbeingData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Combine data from multiple sources
                combine(
                    journalRepository.getAllEntries(),
                    habitRepository.getAllHabits(),
                    workoutRepository.getAllWorkouts()
                ) { journalEntries, habits, workouts ->
                    Triple(journalEntries, habits, workouts)
                }.collect { (journalEntries, habits, workouts) ->
                    
                    // Analyze mood trends from journal entries
                    val moodData = analyzeMoodTrends(journalEntries)
                    
                    // Calculate habit completion rate
                    val habitCompletionRate = calculateHabitCompletionRate(habits)
                    
                    // Analyze workout data
                    val workoutStats = analyzeWorkouts(workouts)
                    
                    // Calculate overall wellbeing score (0-100)
                    val wellbeingScore = calculateWellbeingScore(moodData, habitCompletionRate, workoutStats)
                    
                    // Generate insights
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
                                val workoutDate = workout.startTs
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                workoutDate.isAfter(LocalDate.now().minusWeeks(1))
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun analyzeMoodTrends(entries: List<JournalEntryEntity>): MoodData {
        val last7Days = entries.filter { entry ->
            val entryDate = Instant.ofEpochMilli(entry.date)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            entryDate.isAfter(LocalDate.now().minusDays(7))
        }
        
        val moodCounts = last7Days.groupBy { it.mood ?: "unknown" }
            .mapValues { it.value.size }
        
        val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: "neutral"
        
        val positiveCount = last7Days.count { entry ->
            entry.mood?.lowercase() in listOf("happy", "excited", "grateful", "calm", "loved", "hopeful")
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

    private fun calculateHabitCompletionRate(habits: List<com.ninety5.habitate.data.local.entity.HabitEntity>): Float {
        if (habits.isEmpty()) return 0f
        
        // Filter out archived habits
        val activeHabits = habits.filter { !it.isArchived }
        if (activeHabits.isEmpty()) return 0f
        
        // Note: Actual completion tracking would require checking HabitLogEntity
        // For now, return a placeholder rate
        return 0.5f
    }

    private fun analyzeWorkouts(workouts: List<WorkoutEntity>): WorkoutStats {
        val thisWeek = workouts.filter { workout ->
            val workoutDate = workout.startTs
                .atZone(ZoneId.systemDefault()).toLocalDate()
            workoutDate.isAfter(LocalDate.now().minusWeeks(1))
        }
        
        val totalDuration = thisWeek.sumOf { workout ->
            java.time.Duration.between(workout.startTs, workout.endTs).toMinutes()
        }
        
        val totalCalories = thisWeek.sumOf { it.calories?.toInt() ?: 0 }
        
        return WorkoutStats(
            workoutsThisWeek = thisWeek.size,
            totalMinutes = totalDuration.toInt(),
            totalCalories = totalCalories,
            averageIntensity = if (thisWeek.isNotEmpty()) {
                thisWeek.mapNotNull { it.calories }.average().toFloat()
            } else 0f
        )
    }

    private fun calculateWellbeingScore(
        moodData: MoodData,
        habitCompletionRate: Float,
        workoutStats: WorkoutStats
    ): Int {
        // Weighted scoring
        val moodScore = moodData.positivityRate * 35 // 35% weight
        val habitScore = habitCompletionRate * 35 // 35% weight
        val activityScore = minOf(workoutStats.workoutsThisWeek.toFloat() / 4, 1f) * 30 // 30% weight
        
        return ((moodScore + habitScore + activityScore) * 100).toInt().coerceIn(0, 100)
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
    val recentJournalEntries: List<JournalEntryEntity> = emptyList(),
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
