package com.ninety5.habitate.ui.screens.wellbeing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.LocalHabitateColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToJournal: () -> Unit,
    viewModel: WellbeingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wellbeing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateTheme.colors.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Wellbeing Score Card
                item {
                    WellbeingScoreCard(score = uiState.wellbeingScore)
                }

                // Quick Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Mood,
                            label = "Mood",
                            value = getMoodEmoji(uiState.moodData.dominantMood),
                            subtext = "${uiState.moodData.entriesThisWeek} entries"
                        )
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TrendingUp,
                            label = "Habits",
                            value = "${(uiState.habitCompletionRate * 100).toInt()}%",
                            subtext = "${uiState.activeHabits} active"
                        )
                        QuickStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter,
                            label = "Activity",
                            value = "${uiState.workoutStats.workoutsThisWeek}",
                            subtext = "this week"
                        )
                    }
                }

                // Insights Section
                if (uiState.insights.isNotEmpty()) {
                    item {
                        Text(
                            text = "Insights",
                            style = HabitateTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.insights, key = { "${it.emoji}_${it.title}" }) { insight ->
                                InsightCard(insight = insight)
                            }
                        }
                    }
                }

                // Mood Distribution
                if (uiState.moodData.moodDistribution.isNotEmpty()) {
                    item {
                        MoodDistributionCard(moodDistribution = uiState.moodData.moodDistribution)
                    }
                }

                // Activity Summary
                item {
                    ActivitySummaryCard(workoutStats = uiState.workoutStats)
                }

                // Recent Journal Entries
                if (uiState.recentJournalEntries.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Journal Entries",
                                style = HabitateTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "View all",
                                style = HabitateTheme.typography.labelMedium,
                                color = HabitateTheme.colors.primary,
                                modifier = Modifier.clickable { onNavigateToJournal() }
                            )
                        }
                    }

                    items(uiState.recentJournalEntries, key = { it.id }) { entry ->
                        JournalEntryPreviewCard(entry = entry, onClick = onNavigateToJournal)
                    }
                } else {
                    item {
                        EmptyJournalCard(onNavigateToJournal = onNavigateToJournal)
                    }
                }
            }
        }
    }
}

@Composable
private fun WellbeingScoreCard(score: Int) {
    var animatedScore by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedScore,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )
    
    LaunchedEffect(score) {
        animatedScore = score.toFloat()
    }
    
    val colors = LocalHabitateColors.current
    val scoreColor = when {
        score >= 70 -> colors.success
        score >= 40 -> colors.warning
        else -> colors.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Wellbeing Score",
                style = HabitateTheme.typography.titleMedium,
                color = HabitateTheme.colors.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular progress indicator with score
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    // Background circle
                    drawArc(
                        color = colors.onPrimaryContainer.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = 360f * (animatedValue / 100f),
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = animatedValue.toInt().toString(),
                        style = HabitateTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = HabitateTheme.colors.onPrimaryContainer
                    )
                    Text(
                        text = getScoreLabel(score),
                        style = HabitateTheme.typography.labelMedium,
                        color = HabitateTheme.colors.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getScoreMessage(score),
                style = HabitateTheme.typography.bodyMedium,
                color = HabitateTheme.colors.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HabitateTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = HabitateTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                style = HabitateTheme.typography.labelSmall,
                color = HabitateTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightCard(insight: WellbeingInsight) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = insight.emoji,
                style = HabitateTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = insight.title,
                    style = HabitateTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = HabitateTheme.typography.bodySmall,
                    color = HabitateTheme.colors.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MoodDistributionCard(moodDistribution: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mood Distribution",
                style = HabitateTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val total = moodDistribution.values.sum().toFloat()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moodDistribution.entries.sortedByDescending { it.value }.take(5).forEach { (mood, count) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getMoodEmoji(mood),
                            style = HabitateTheme.typography.titleMedium
                        )
                        Text(
                            text = if (total > 0) "${((count / total) * 100).toInt()}%" else "0%",
                            style = HabitateTheme.typography.labelSmall,
                            color = HabitateTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySummaryCard(workoutStats: WorkoutStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.accentContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HabitateTheme.colors.onAccentContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Activity Summary",
                    style = HabitateTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${workoutStats.workoutsThisWeek}",
                        style = HabitateTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "workouts",
                        style = HabitateTheme.typography.labelSmall,
                        color = HabitateTheme.colors.onAccentContainer.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${workoutStats.totalMinutes}",
                        style = HabitateTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "minutes",
                        style = HabitateTheme.typography.labelSmall,
                        color = HabitateTheme.colors.onAccentContainer.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${workoutStats.totalCalories}",
                        style = HabitateTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "calories",
                        style = HabitateTheme.typography.labelSmall,
                        color = HabitateTheme.colors.onAccentContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalEntryPreviewCard(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val date = remember(entry.createdAt) {
        entry.createdAt
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mood emoji
            Text(
                text = getMoodEmoji(entry.mood?.name?.lowercase() ?: "neutral"),
                style = HabitateTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                entry.title?.let { title ->
                    Text(
                        text = title,
                        style = HabitateTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = entry.content,
                    style = HabitateTheme.typography.bodySmall,
                    color = HabitateTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = date.format(dateFormatter),
                style = HabitateTheme.typography.labelSmall,
                color = HabitateTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyJournalCard(onNavigateToJournal: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToJournal() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = HabitateTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Start Journaling",
                style = HabitateTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your mood and thoughts to improve your wellbeing",
                style = HabitateTheme.typography.bodySmall,
                color = HabitateTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getMoodEmoji(mood: String): String {
    return when (mood.lowercase()) {
        "happy" -> "😊"
        "calm" -> "😌"
        "anxious" -> "😰"
        "sad" -> "😢"
        "excited" -> "🤩"
        "tired" -> "😴"
        "grateful" -> "🙏"
        "frustrated" -> "😤"
        "angry" -> "😠"
        "loved" -> "🥰"
        "hopeful" -> "🌟"
        "confused" -> "😕"
        "neutral" -> "😐"
        else -> "😐"
    }
}

private fun getScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Moderate"
        score >= 20 -> "Low"
        else -> "Needs attention"
    }
}

private fun getScoreMessage(score: Int): String {
    return when {
        score >= 80 -> "You're doing amazing! Keep up the great work on your wellness journey."
        score >= 60 -> "Good progress! A few small changes can boost your wellbeing even more."
        score >= 40 -> "You're on the right track. Focus on consistency with your habits."
        score >= 20 -> "Room for improvement. Start with one small healthy habit today."
        else -> "Let's work together to improve your wellbeing. Every small step counts!"
    }
}
