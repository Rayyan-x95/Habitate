package com.ninety5.habitate.ui.screens.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ninety5.habitate.ui.theme.HabitateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Scaffold(
        containerColor = HabitateTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Workout Details", color = HabitateTheme.colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = HabitateTheme.colors.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = HabitateTheme.colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateTheme.colors.background,
                    titleContentColor = HabitateTheme.colors.onBackground,
                    navigationIconContentColor = HabitateTheme.colors.onBackground,
                    actionIconContentColor = HabitateTheme.colors.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Workout Detail: $workoutId", color = HabitateTheme.colors.onBackground)
        }
    }
}


