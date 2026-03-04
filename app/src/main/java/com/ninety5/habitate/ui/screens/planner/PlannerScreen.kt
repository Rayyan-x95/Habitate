package com.ninety5.habitate.ui.screens.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.LocalHabitateColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    onBackClick: () -> Unit,
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "This Week", "Suggestions")

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PlannerEvent.TaskAccepted -> snackbarHostState.showSnackbar("Task added to your list!")
                is PlannerEvent.TaskDismissed -> snackbarHostState.showSnackbar("Suggestion dismissed")
                is PlannerEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Planner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HabitateTheme.colors.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (uiState) {
                is PlannerUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzing your habits and tasks...",
                                style = HabitateTheme.typography.bodyMedium,
                                color = HabitateTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                is PlannerUiState.Success -> {
                    val state = uiState as PlannerUiState.Success
                    val selectedDate by viewModel.selectedDate.collectAsState()
                    
                    when (selectedTab) {
                        0 -> TodayPlanView(
                            dailyAdvice = state.dailyAdvice,
                            todayTasks = state.todayTasks,
                            todayHabits = state.todayHabits,
                            selectedDate = selectedDate,
                            onTaskComplete = { viewModel.completeTask(it) }
                        )
                        1 -> WeeklyPlanView(
                            weeklyPlan = state.weeklyPlan,
                            onDaySelected = { viewModel.selectDay(it) }
                        )
                        2 -> SuggestionsView(
                            suggestions = state.suggestions,
                            onAccept = { viewModel.acceptSuggestion(it) },
                            onDismiss = { viewModel.dismissSuggestion(it) }
                        )
                    }
                }

                is PlannerUiState.Error -> {
                    ErrorView(
                        message = (uiState as PlannerUiState.Error).message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                is PlannerUiState.Offline -> {
                    OfflineView(
                        cachedAdvice = (uiState as PlannerUiState.Offline).cachedAdvice,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayPlanView(
    dailyAdvice: String,
    todayTasks: List<PlannedTask>,
    todayHabits: List<PlannedHabit>,
    selectedDate: LocalDate,
    onTaskComplete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Daily Advice Card
        item {
            DailyAdviceCard(advice = dailyAdvice)
        }

        // Today's schedule header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = HabitateTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = HabitateTheme.typography.labelMedium,
                    color = HabitateTheme.colors.onSurfaceVariant
                )
            }
        }

        // Tasks
        if (todayTasks.isEmpty() && todayHabits.isEmpty()) {
            item {
                EmptyScheduleCard()
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                PlannedTaskCard(
                    task = task,
                    onComplete = { onTaskComplete(task.id) }
                )
            }

            if (todayHabits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Habits to complete",
                        style = HabitateTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                items(todayHabits, key = { it.id }) { habit ->
                    PlannedHabitCard(habit = habit)
                }
            }
        }
    }
}

@Composable
private fun DailyAdviceCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "AI Insight",
                    tint = HabitateTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Insight",
                    style = HabitateTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HabitateTheme.colors.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = advice,
                style = HabitateTheme.typography.bodyMedium,
                color = HabitateTheme.colors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PlannedTaskCard(
    task: PlannedTask,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                HabitateTheme.colors.surfaceVariant.copy(alpha = 0.3f)
                else HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) LocalHabitateColors.current.success.copy(alpha = 0.2f)
                            else HabitateTheme.colors.primary.copy(alpha = 0.1f)
                        )
                        .clickable { if (!task.isCompleted) onComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = LocalHabitateColors.current.success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = task.title,
                        style = HabitateTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) 
                            HabitateTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                            else HabitateTheme.colors.onSurfaceVariant
                    )
                    task.scheduledTime?.let { time ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Scheduled time",
                                modifier = Modifier.size(14.dp),
                                tint = HabitateTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = time,
                                style = HabitateTheme.typography.labelSmall,
                                color = HabitateTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            task.priority?.let { priority ->
                PriorityChip(priority = priority)
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: TaskPriority) {
    val colors = LocalHabitateColors.current
    val (color, text) = when (priority) {
        TaskPriority.HIGH -> colors.error to "High"
        TaskPriority.MEDIUM -> colors.warning to "Med"
        TaskPriority.LOW -> colors.success to "Low"
    }
    
    Text(
        text = text,
        style = HabitateTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun PlannedHabitCard(habit: PlannedHabit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habit.emoji,
                    style = HabitateTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = habit.name,
                        style = HabitateTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "🔥 ${habit.streak} day streak",
                        style = HabitateTheme.typography.labelSmall,
                        color = HabitateTheme.colors.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (habit.isCompletedToday) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = LocalHabitateColors.current.success
                )
            }
        }
    }
}

@Composable
private fun WeeklyPlanView(
    weeklyPlan: List<DayPlan>,
    onDaySelected: (LocalDate) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Weekly Overview",
                style = HabitateTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(weeklyPlan, key = { it.date.toString() }) { dayPlan ->
            DayPlanCard(
                dayPlan = dayPlan,
                onClick = { onDaySelected(dayPlan.date) }
            )
        }
    }
}

@Composable
private fun DayPlanCard(
    dayPlan: DayPlan,
    onClick: () -> Unit
) {
    val isToday = dayPlan.date == LocalDate.now()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) 
                HabitateTheme.colors.primaryContainer.copy(alpha = 0.5f)
                else HabitateTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = dayPlan.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = HabitateTheme.typography.labelMedium,
                    color = if (isToday) HabitateTheme.colors.primary 
                            else HabitateTheme.colors.onSurfaceVariant
                )
                Text(
                    text = dayPlan.date.dayOfMonth.toString(),
                    style = HabitateTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) HabitateTheme.colors.primary
                            else HabitateTheme.colors.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${dayPlan.taskCount} tasks • ${dayPlan.habitCount} habits",
                    style = HabitateTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (dayPlan.focusArea != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Focus: ${dayPlan.focusArea}",
                        style = HabitateTheme.typography.labelSmall,
                        color = HabitateTheme.colors.onSurfaceVariant
                    )
                }
            }
            
            // Completion indicator
            val completionColor = LocalHabitateColors.current
            CircularProgressIndicator(
                progress = { dayPlan.completionRate },
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = when {
                    dayPlan.completionRate >= 0.8f -> completionColor.success
                    dayPlan.completionRate >= 0.5f -> completionColor.warning
                    else -> HabitateTheme.colors.border
                }
            )
        }
    }
}

@Composable
private fun SuggestionsView(
    suggestions: List<AISuggestion>,
    onAccept: (AISuggestion) -> Unit,
    onDismiss: (AISuggestion) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = HabitateTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Suggestions",
                    style = HabitateTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Based on your habits and patterns",
                style = HabitateTheme.typography.bodySmall,
                color = HabitateTheme.colors.onSurfaceVariant
            )
        }

        if (suggestions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Text("✨", style = HabitateTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No suggestions right now",
                            style = HabitateTheme.typography.titleSmall
                        )
                        Text(
                            text = "Keep using the app and I'll learn your patterns!",
                            style = HabitateTheme.typography.bodySmall,
                            color = HabitateTheme.colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(suggestions, key = { it.id }) { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    onAccept = { onAccept(suggestion) },
                    onDismiss = { onDismiss(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: AISuggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateTheme.colors.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = suggestion.emoji,
                    style = HabitateTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = HabitateTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suggestion.description,
                        style = HabitateTheme.typography.bodySmall,
                        color = HabitateTheme.colors.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    
                    suggestion.reason?.let { reason ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Why: $reason",
                            style = HabitateTheme.typography.labelSmall,
                            color = HabitateTheme.colors.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss suggestion", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
                Button(onClick = onAccept) {
                    Icon(Icons.Default.Add, contentDescription = "Accept suggestion", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun EmptyScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text("📋", style = HabitateTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your schedule is clear!",
                style = HabitateTheme.typography.titleSmall
            )
            Text(
                text = "Add tasks or habits to get AI-powered planning",
                style = HabitateTheme.typography.bodySmall,
                color = HabitateTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❌", style = HabitateTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = HabitateTheme.typography.bodyMedium,
                color = HabitateTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun OfflineView(
    cachedAdvice: String?,
    onRetry: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HabitateTheme.colors.accentContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📡", style = HabitateTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You're offline",
                            style = HabitateTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Showing cached advice",
                            style = HabitateTheme.typography.bodySmall,
                            color = HabitateTheme.colors.onAccentContainer.copy(alpha = 0.7f)
                        )
                    }
                    OutlinedButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }

        cachedAdvice?.let { advice ->
            item {
                DailyAdviceCard(advice = advice)
            }
        }
    }
}
