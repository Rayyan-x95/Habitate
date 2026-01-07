package com.ninety5.habitate.ui.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.theme.HabitateDarkGreenStart
import com.ninety5.habitate.ui.theme.HabitateOffWhite
import com.ninety5.habitate.ui.theme.SageGreen
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    val task by viewModel.task.collectAsState()

    Scaffold(
        containerColor = HabitateDarkGreenStart,
        topBar = {
            TopAppBar(
                title = { Text("Task Details", color = HabitateOffWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HabitateOffWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateDarkGreenStart,
                    titleContentColor = HabitateOffWhite,
                    navigationIconContentColor = HabitateOffWhite
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val currentTask = task
            if (currentTask != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentTask.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = HabitateOffWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    currentTask.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = HabitateOffWhite.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    currentTask.dueAt?.let { dueAt ->
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                        Text(
                            text = "Due: ${formatter.format(dueAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SageGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Status: ${currentTask.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HabitateOffWhite.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text("Loading...", modifier = Modifier.padding(16.dp), color = HabitateOffWhite)
            }
        }
    }
}
