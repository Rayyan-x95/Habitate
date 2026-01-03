package com.ninety5.habitate.ui.screens.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInScreen(
    onDismiss: () -> Unit,
    viewModel: DailyCheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSaved) {
        LaunchedEffect(Unit) {
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check-in") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveCheckIn() }) {
                        Icon(Icons.Rounded.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How are you feeling today?", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            val moods = listOf("Happy", "Energetic", "Calm", "Stressed", "Tired", "Sad")
            
            moods.chunked(3).forEach { rowMoods ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowMoods.forEach { mood ->
                        FilterChip(
                            selected = uiState.mood == mood,
                            onClick = { viewModel.setMood(mood) },
                            label = { Text(mood) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = uiState.notes ?: "",
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
        }
    }
}
