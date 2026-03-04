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
import com.ninety5.habitate.ui.theme.HabitateTheme
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
        containerColor = HabitateTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Task Details", color = HabitateTheme.colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HabitateTheme.colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateTheme.colors.background,
                    titleContentColor = HabitateTheme.colors.onBackground,
                    navigationIconContentColor = HabitateTheme.colors.onBackground
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
                        style = HabitateTheme.typography.headlineMedium,
                        color = HabitateTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    currentTask.description?.let { description ->
                        Text(
                            text = description,
                            style = HabitateTheme.typography.bodyLarge,
                            color = HabitateTheme.colors.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    currentTask.dueAt?.let { dueAt ->
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                        Text(
                            text = "Due: ${formatter.format(dueAt)}",
                            style = HabitateTheme.typography.bodyMedium,
                            color = HabitateTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Status: ${currentTask.status}",
                        style = HabitateTheme.typography.bodyMedium,
                        color = HabitateTheme.colors.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text("Loading...", modifier = Modifier.padding(16.dp), color = HabitateTheme.colors.onBackground)
            }
        }
    }
}
