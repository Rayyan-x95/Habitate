package com.ninety5.habitate.ui.screens.habit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.domain.model.HabitMood
import com.ninety5.habitate.ui.components.*
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateErrorState
import com.ninety5.habitate.ui.components.designsystem.HabitateLoadingScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.ninety5.habitate.ui.theme.HabitateTheme

/**
 * Habit Detail Screen - Complete view of a single habit.
 * 
 * Features:
 * - Habit header with category & icon
 * - Current/longest streak display
 * - 365-day heatmap calendar
 * - Completion history with mood tracking
 * - Quick complete/uncomplete
 * - Edit/Delete actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoodDialog by remember { mutableStateOf(false) }

    // Handle habit deleted
    LaunchedEffect(uiState.habitDeleted) {
        if (uiState.habitDeleted) {
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteHabit()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showMoodDialog && uiState.habitWithDetails != null) {
        MoodSelectionDialog(
            onMoodSelected = { mood, note ->
                viewModel.completeHabit(mood, note)
                showMoodDialog = false
            },
            onDismiss = { showMoodDialog = false }
        )
    }

    Scaffold(
        containerColor = HabitateTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(uiState.habitWithDetails?.habit?.title ?: "Habit", color = HabitateTheme.colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateTheme.colors.background,
                    navigationIconContentColor = HabitateTheme.colors.onBackground,
                    actionIconContentColor = HabitateTheme.colors.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        onEditHabit(habitId)
                    }) {
                        Icon(Icons.Rounded.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Rounded.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> HabitateLoadingScreen()
            
            uiState.error != null -> HabitateErrorState(
                title = "Something went wrong",
                description = uiState.error ?: "Unknown error",
                onRetry = { viewModel.retry() }
            )
            
            uiState.habitWithDetails != null -> uiState.habitWithDetails?.let { habitWithDetails ->
                val habit = habitWithDetails.habit
                val logs = habitWithDetails.recentLogs
                val streak = uiState.streak
                
                // Calculate if completed today
                val today = java.time.LocalDate.now().toString()
                val isCompletedToday = logs.any { log ->
                    log.completedAt.toString().substringBefore('T') == today
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        HabitHeaderCard(
                            habit = habit,
                            streak = streak,
                            isCompletedToday = isCompletedToday,
                            onCompleteClick = {
                                if (isCompletedToday) {
                                    viewModel.uncompleteHabit(java.time.LocalDate.now().toString())
                                } else {
                                    showMoodDialog = true
                                }
                            }
                        )
                    }

                    // Success animation
                    item {
                        AnimatedVisibility(
                            visible = uiState.showCompletionSuccess,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = HabitateTheme.colors.primary
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🎉", style = HabitateTheme.typography.headlineMedium)
                                    Text(
                                        "Great job! Keep it up!",
                                        style = HabitateTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = HabitateTheme.colors.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Heatmap Calendar
                    item {
                        Text(
                            "Activity",
                            style = HabitateTheme.typography.titleMedium,
                            color = HabitateTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        HeatmapCalendar(
                            heatmapData = uiState.heatmapData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Completion History
                    item {
                        Text(
                            "History",
                            style = HabitateTheme.typography.titleMedium,
                            color = HabitateTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (logs.isEmpty()) {
                        item {
                            HabitateEmptyState(
                                title = "No completions yet",
                                description = "Start completing this habit to see your progress",
                                icon = Icons.Rounded.EventBusy
                            )
                        }
                    } else {
                        items(logs, key = { it.id }) { log ->
                            CompletionLogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitHeaderCard(
    habit: com.ninety5.habitate.domain.model.Habit,
    streak: com.ninety5.habitate.domain.model.HabitStreak?,
    isCompletedToday: Boolean,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon + Title
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = try {
                            Color(android.graphics.Color.parseColor(habit.color))
                        } catch (e: Exception) {
                            HabitateTheme.colors.primary.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(habit.icon, style = HabitateTheme.typography.titleLarge)
                        }
                    }
                    
                    Column {
                        Text(
                            habit.title,
                            style = HabitateTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HabitateTheme.colors.onSurface
                        )
                        Text(
                            habit.category.getDisplayName(),
                            style = HabitateTheme.typography.bodyMedium,
                            color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Complete button
                FilledTonalButton(
                    onClick = onCompleteClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isCompletedToday)
                            HabitateTheme.colors.primary
                        else
                            HabitateTheme.colors.onSurface.copy(alpha = 0.1f),
                        contentColor = if (isCompletedToday) HabitateTheme.colors.onPrimary else HabitateTheme.colors.onSurface
                    )
                ) {
                    Icon(
                        if (isCompletedToday) Icons.Rounded.Check else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isCompletedToday) "Done" else "Complete")
                }
            }

            if (habit.description?.isNotBlank() == true) {
                Spacer(Modifier.height(12.dp))
                Text(
                    habit.description,
                    style = HabitateTheme.typography.bodyMedium,
                    color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            // Streak display
            if (streak != null) {
                Spacer(Modifier.height(16.dp))
                StreakBadge(
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak
                )
            }
        }
    }
}

@Composable
private fun HeatmapCalendar(
    heatmapData: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Last 365 days",
                style = HabitateTheme.typography.labelMedium,
                color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Simple grid representation (7 columns for weeks)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val sortedDates = heatmapData.keys.sorted()
                items(sortedDates, key = { it.toString() }) { date ->
                    val count = heatmapData[date] ?: 0
                    val intensity = when {
                        count == 0 -> 0.0f
                        count >= 3 -> 1.0f
                        else -> count / 3f
                    }
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                HabitateTheme.colors.primary.copy(
                                    alpha = if (count > 0) 0.2f + (intensity * 0.8f) else 0.1f
                                )
                            )
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Less",
                    style = HabitateTheme.typography.labelSmall,
                    color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                HabitateTheme.colors.primary.copy(
                                    alpha = 0.1f + (index * 0.2f)
                                )
                            )
                    )
                }
                Text(
                    "More",
                    style = HabitateTheme.typography.labelSmall,
                    color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CompletionLogCard(
    log: com.ninety5.habitate.domain.model.HabitLog,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HabitateTheme.colors.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    log.completedAt.toString().substringBefore('T'),
                    style = HabitateTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = HabitateTheme.colors.onSurface
                )
                if (log.note?.isNotBlank() == true) {
                    Text(
                        log.note,
                        style = HabitateTheme.typography.bodyMedium,
                        color = HabitateTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (log.mood != null) {
                Text(
                    log.mood.getEmoji(),
                    style = HabitateTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Composable
private fun MoodSelectionDialog(
    onMoodSelected: (HabitMood, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        containerColor = HabitateTheme.colors.surface,
        titleContentColor = HabitateTheme.colors.onSurface,
        textContentColor = HabitateTheme.colors.onSurface,
        onDismissRequest = onDismiss,
        title = { Text("How did it go?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HabitMood.entries.forEach { mood ->
                        Surface(
                            onClick = { onMoodSelected(mood, notes.takeIf { it.isNotBlank() }) },
                            shape = RoundedCornerShape(12.dp),
                            color = HabitateTheme.colors.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    mood.getEmoji(),
                                    style = HabitateTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HabitateTheme.colors.onSurface,
                        unfocusedTextColor = HabitateTheme.colors.onSurface,
                        focusedBorderColor = HabitateTheme.colors.primary,
                        unfocusedBorderColor = HabitateTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = HabitateTheme.colors.primary,
                        unfocusedLabelColor = HabitateTheme.colors.onSurface.copy(alpha = 0.7f),
                        cursorColor = HabitateTheme.colors.primary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onMoodSelected(HabitMood.NEUTRAL, notes.takeIf { it.isNotBlank() }) },
                colors = ButtonDefaults.textButtonColors(contentColor = HabitateTheme.colors.primary)
            ) {
                Text("Skip")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = HabitateTheme.colors.onSurface.copy(alpha = 0.7f))
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = HabitateTheme.colors.surface,
        titleContentColor = HabitateTheme.colors.onSurface,
        textContentColor = HabitateTheme.colors.onSurface,
        iconContentColor = HabitateTheme.colors.onSurface,
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Warning, null) },
        title = { Text("Delete habit?") },
        text = { Text("This will permanently delete this habit and all its completion history. This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HabitateTheme.colors.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = HabitateTheme.colors.onSurface.copy(alpha = 0.7f))
            ) {
                Text("Cancel")
            }
        }
    )
}
