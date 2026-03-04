package com.ninety5.habitate.ui.screens.workout

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.model.WorkoutSource
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateFab
import com.ninety5.habitate.ui.components.designsystem.HabitateLargeTopBar
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.ShimmerBox
import com.ninety5.habitate.ui.components.designsystem.ShimmerLine
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onWorkoutClick: (String) -> Unit,
    onCreateWorkoutClick: () -> Unit,
    onNavigateToHealthConnect: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = HabitateTheme.colors
    val typography = HabitateTheme.typography

    LaunchedEffect(uiState.importStatus) {
        uiState.importStatus?.let { status ->
            snackbarHostState.showSnackbar(status)
            viewModel.clearImportStatus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HabitateLargeTopBar(
                title = "Workouts",
                actions = {
                    IconButton(onClick = { viewModel.importHealthConnectWorkouts() }) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorHeart,
                            contentDescription = "Import from Health Connect",
                            tint = colors.primary
                        )
                    }
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                // Loading skeleton
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.isLoading && workouts.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    WorkoutListSkeleton()
                }

                // Empty state
                androidx.compose.animation.AnimatedVisibility(
                    visible = workouts.isEmpty() && !uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        HabitateEmptyState(
                            icon = Icons.Rounded.FitnessCenter,
                            title = "No workouts logged yet",
                            description = "Start tracking your fitness journey"
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        HabitatePrimaryButton(
                            text = "Connect Health Data",
                            onClick = onNavigateToHealthConnect
                        )
                    }
                }

                // Content
                androidx.compose.animation.AnimatedVisibility(
                    visible = workouts.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal,
                            top = Spacing.sm,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(workouts, key = { it.id }) { workout ->
                            WorkoutItem(
                                workout = workout,
                                onClick = { onWorkoutClick(workout.id) }
                            )
                        }
                    }
                }
            }
        }

        // FAB
        HabitateFab(
            icon = Icons.Rounded.Add,
            onClick = onCreateWorkoutClick,
            contentDescription = "Log Workout",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.screenHorizontal, bottom = 96.dp)
        )

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WorkoutListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(6) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(Radius.md)
            )
        }
    }
}

@Composable
fun WorkoutItem(
    workout: Workout,
    onClick: () -> Unit
) {
    val colors = HabitateTheme.colors
    val typography = HabitateTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(Radius.md)
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Size.iconXl)
                    .clip(RoundedCornerShape(Radius.md / 2))
                    .background(colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(Size.iconMd)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.md)
            ) {
                Text(
                    text = workout.type.name,
                    style = typography.titleMedium,
                    color = colors.onBackground
                )
                Text(
                    text = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(workout.startTime),
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.6f)
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    if (workout.distanceMeters != null && workout.distanceMeters > 0) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f km", workout.distanceMeters / 1000.0),
                            style = typography.labelMedium,
                            color = colors.primary,
                            modifier = Modifier.padding(end = Spacing.sm)
                        )
                    }
                    if (workout.caloriesBurned != null && workout.caloriesBurned > 0) {
                        Text(
                            text = String.format(java.util.Locale.US, "%d kcal", workout.caloriesBurned.toInt()),
                            style = typography.labelMedium,
                            color = colors.primary
                        )
                    }
                }
            }

            if (workout.source == WorkoutSource.HEALTH_CONNECT) {
                Icon(
                    imageVector = Icons.Rounded.MonitorHeart,
                    contentDescription = "Health Connect",
                    tint = colors.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(Size.iconSm)
                )
            }
        }
    }
}
