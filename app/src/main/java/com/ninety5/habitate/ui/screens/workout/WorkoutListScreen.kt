package com.ninety5.habitate.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.local.entity.WorkoutSource
import com.ninety5.habitate.ui.theme.HabitateDarkGreenStart
import com.ninety5.habitate.ui.theme.HabitateOffWhite
import com.ninety5.habitate.ui.theme.SageGreen
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
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(uiState.importStatus) {
        uiState.importStatus?.let { status ->
            snackbarHostState.showSnackbar(status)
            viewModel.clearImportStatus()
        }
    }

    Scaffold(
        containerColor = HabitateDarkGreenStart,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Workouts", color = HabitateOffWhite) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateDarkGreenStart,
                    titleContentColor = HabitateOffWhite
                ),
                actions = {
                    IconButton(onClick = { viewModel.importHealthConnectWorkouts() }) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorHeart,
                            contentDescription = "Import from Health Connect",
                            tint = SageGreen
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateWorkoutClick,
                containerColor = SageGreen,
                contentColor = HabitateDarkGreenStart
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Log Workout")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (workouts.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No workouts logged yet", color = HabitateOffWhite)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToHealthConnect,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = HabitateDarkGreenStart)
                        ) {
                            Text("Connect Health Data")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workouts, key = { it.id }) { workout ->
                        WorkoutItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) }
                        )
                    }
                }
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun WorkoutItem(
    workout: WorkoutEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = HabitateDarkGreenStart.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(40.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = workout.type,
                    style = MaterialTheme.typography.titleMedium,
                    color = HabitateOffWhite
                )
                Text(
                    text = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(workout.startTs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitateOffWhite.copy(alpha = 0.7f)
                )
                
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    if (workout.distanceMeters != null && workout.distanceMeters > 0) {
                        Text(
                            text = String.format("%.2f km", workout.distanceMeters / 1000.0),
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    if (workout.calories != null && workout.calories > 0) {
                        Text(
                            text = "${workout.calories.toInt()} kcal",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen
                        )
                    }
                }
            }
            
            if (workout.source == WorkoutSource.HEALTH_CONNECT) {
                Icon(
                    imageVector = Icons.Rounded.MonitorHeart,
                    contentDescription = "Health Connect",
                    tint = SageGreen.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
