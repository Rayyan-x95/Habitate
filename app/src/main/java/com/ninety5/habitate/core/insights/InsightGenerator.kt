package com.ninety5.habitate.core.insights

import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.DailySummaryDao
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.HabitLogDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.entity.HabitFrequency
import com.ninety5.habitate.data.local.entity.InsightEntity
import com.ninety5.habitate.data.local.entity.InsightPriority
import com.ninety5.habitate.data.local.entity.InsightType
import com.ninety5.habitate.data.repository.HabitRepository
import com.ninety5.habitate.data.repository.InsightRepository
import com.ninety5.habitate.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightGenerator @Inject constructor(
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository,
    private val insightRepository: InsightRepository,
    private val dailySummaryDao: DailySummaryDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val securePreferences: SecurePreferences
) {

    suspend fun generateInsights() {
        generateStreakRiskInsights()
        generateMilestoneInsights()
        generateInactivityInsights()
        generateWeeklySummaries()
        generateTaskFailureInsights()
        generateHabitFrictionInsights()
        generateEnergyTrendInsights()
    }

    private suspend fun generateStreakRiskInsights() {
        val habitsWithStreaks = habitRepository.getActiveHabitsWithStreaks().first()
        val today = LocalDate.now()

        habitsWithStreaks.forEach { habitWithStreak ->
            val streak = habitWithStreak.streak
            if (streak != null && streak.currentStreak > 3) {
                // Check if completed today
                // This logic assumes we can check if it's done today. 
                // HabitWithStreak usually contains the habit and the streak info.
                // We might need to check the last log date.
                
                val lastCompletedDateStr = streak.lastCompletedDate
                if (lastCompletedDateStr != null) {
                    val lastCompletedDate = LocalDate.parse(lastCompletedDateStr)
                    if (lastCompletedDate.isBefore(today)) {
                        // Not done today yet
                        // If it's late in the day (e.g. after 8 PM), generate a risk insight
                        val currentHour = java.time.LocalTime.now().hour
                        if (currentHour >= 20) {
                            val insight = InsightEntity(
                                id = UUID.randomUUID().toString(),
                                type = InsightType.STREAK_RISK,
                                title = "Streak at Risk!",
                                description = "You have a ${streak.currentStreak}-day streak for '${habitWithStreak.habit.title}'. Don't break it now!",
                                priority = InsightPriority.HIGH,
                                relatedEntityId = habitWithStreak.habit.id,
                                createdAt = Instant.now()
                            )
                            insightRepository.addInsight(insight)
                        }
                    }
                }
            }
        }
    }

    private suspend fun generateMilestoneInsights() {
        val habitsWithStreaks = habitRepository.getActiveHabitsWithStreaks().first()
        
        habitsWithStreaks.forEach { habitWithStreak ->
            val streak = habitWithStreak.streak
            if (streak != null) {
                val current = streak.currentStreak
                val nextMilestone = getNextMilestone(current)
                
                if (nextMilestone - current == 1) {
                    val insight = InsightEntity(
                        id = UUID.randomUUID().toString(),
                        type = InsightType.MILESTONE_APPROACHING,
                        title = "Almost there!",
                        description = "Just 1 more day to reach a $nextMilestone-day streak for '${habitWithStreak.habit.title}'!",
                        priority = InsightPriority.MEDIUM,
                        relatedEntityId = habitWithStreak.habit.id,
                        createdAt = Instant.now()
                    )
                    insightRepository.addInsight(insight)
                }
            }
        }
    }

    private fun getNextMilestone(current: Int): Int {
        return when {
            current < 7 -> 7
            current < 14 -> 14
            current < 21 -> 21
            current < 30 -> 30
            current < 50 -> 50
            current < 100 -> 100
            else -> ((current / 100) + 1) * 100
        }
    }

    private suspend fun generateInactivityInsights() {
        val workouts = workoutRepository.getAllWorkouts().first()
        if (workouts.isEmpty()) return

        val lastWorkout = workouts.maxByOrNull { it.endTs } ?: return
        val daysSinceLastWorkout = java.time.Duration.between(lastWorkout.endTs, Instant.now()).toDays()

        if (daysSinceLastWorkout > 3) {
            val insight = InsightEntity(
                id = UUID.randomUUID().toString(),
                type = InsightType.SUGGESTION,
                title = "Time to move?",
                description = "It's been $daysSinceLastWorkout days since your last workout. A quick session can boost your energy!",
                priority = InsightPriority.MEDIUM,
                relatedEntityId = null,
                createdAt = Instant.now()
            )
            insightRepository.addInsight(insight)
        }
    }

    private suspend fun generateWeeklySummaries() {
        val today = LocalDate.now()
        // Only generate on Sundays
        if (today.dayOfWeek != java.time.DayOfWeek.SUNDAY) return

        val startOfWeek = today.minusDays(6)
        val summaries = dailySummaryDao.getSummariesBetween(startOfWeek, today)
        
        if (summaries.isNotEmpty()) {
            val totalSteps = summaries.sumOf { it.steps }
            val avgMood = summaries.mapNotNull { it.mood }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            
            val insight = InsightEntity(
                id = UUID.randomUUID().toString(),
                type = InsightType.WEEKLY_SUMMARY,
                title = "Weekly Summary",
                description = "You walked $totalSteps steps this week. Your dominant mood was ${avgMood ?: "stable"}.",
                priority = InsightPriority.LOW,
                relatedEntityId = null,
                createdAt = Instant.now()
            )
            insightRepository.addInsight(insight)
        }
    }

    private suspend fun generateTaskFailureInsights() {
        val overdueTasks = taskDao.getOverdueTasks(Instant.now().toEpochMilli())
        if (overdueTasks.isNotEmpty()) {
            val count = overdueTasks.size
            val insight = InsightEntity(
                id = UUID.randomUUID().toString(),
                type = InsightType.TASK_FAILURE,
                title = "Overdue Tasks",
                description = "You have $count overdue tasks. Consider rescheduling them or breaking them down.",
                priority = InsightPriority.MEDIUM,
                relatedEntityId = null,
                createdAt = Instant.now()
            )
            insightRepository.addInsight(insight)
        }
    }

    private suspend fun generateHabitFrictionInsights() {
        val userId = securePreferences.userId ?: return
        val activeHabits = habitDao.getActiveHabitsOnce(userId)
        val end = System.currentTimeMillis()
        val start = end - 14 * 24 * 60 * 60 * 1000L // 14 days ago

        val logs = habitLogDao.getLogsBetween(userId, start, end)
        val logsByHabit = logs.groupBy { it.habitId }

        activeHabits.forEach { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val actualCompletions = habitLogs.size
            
            val expectedCompletions = when (habit.frequency) {
                HabitFrequency.DAILY -> 14
                HabitFrequency.WEEKLY -> 2
                HabitFrequency.CUSTOM -> {
                    val daysPerWeek = habit.customSchedule?.size ?: 0
                    daysPerWeek * 2
                }
            }

            if (expectedCompletions > 0) {
                val completionRate = actualCompletions.toDouble() / expectedCompletions
                val isOldEnough = habit.createdAt.toEpochMilli() < start
                
                if (isOldEnough && completionRate < 0.4) {
                    val insight = InsightEntity(
                        id = UUID.randomUUID().toString(),
                        type = InsightType.HABIT_FRICTION,
                        title = "Struggling with '${habit.title}'?",
                        description = "You've completed this habit only $actualCompletions times in the last 2 weeks. Consider reducing the frequency or making it easier.",
                        priority = InsightPriority.MEDIUM,
                        relatedEntityId = habit.id,
                        createdAt = Instant.now()
                    )
                    insightRepository.addInsight(insight)
                }
            }
        }
    }

    private suspend fun generateEnergyTrendInsights() {
        val end = LocalDate.now()
        val start = end.minusDays(30)
        val summaries = dailySummaryDao.getSummariesBetween(start, end)
        
        if (summaries.size < 10) return // Not enough data

        val moodScores = mapOf(
            "Happy" to 3, "Energetic" to 3, "Great" to 3,
            "Good" to 2, "Okay" to 1, "Neutral" to 1,
            "Stressed" to 0, "Sad" to 0, "Tired" to 0, "Bad" to 0
        )

        val dataPoints = summaries.mapNotNull { summary ->
            val mood = summary.mood ?: return@mapNotNull null
            val score = moodScores[mood] ?: return@mapNotNull null
            summary.activeMinutes to score
        }

        if (dataPoints.size < 5) return

        val avgActivity = dataPoints.map { it.first }.average()
        
        val highActivityMoods = dataPoints.filter { it.first > avgActivity }.map { it.second }
        val lowActivityMoods = dataPoints.filter { it.first <= avgActivity }.map { it.second }

        if (highActivityMoods.isNotEmpty() && lowActivityMoods.isNotEmpty()) {
            val highAvg = highActivityMoods.average()
            val lowAvg = lowActivityMoods.average()

            if (highAvg > lowAvg + 0.5) {
                val insight = InsightEntity(
                    id = UUID.randomUUID().toString(),
                    type = InsightType.ENERGY_TREND,
                    title = "Movement Boosts Mood",
                    description = "On days you are more active, your mood tends to be better. Keep moving!",
                    priority = InsightPriority.LOW,
                    relatedEntityId = null,
                    createdAt = Instant.now()
                )
                insightRepository.addInsight(insight)
            }
        }
    }
}
