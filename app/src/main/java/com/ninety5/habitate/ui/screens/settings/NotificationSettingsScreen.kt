package com.ninety5.habitate.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pushEnabled = uiState.notificationsEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Master toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pushEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (pushEnabled) Icons.Default.NotificationsActive
                            else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Push Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (pushEnabled) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = viewModel::toggleNotifications
                    )
                }
            }

            // Categories
            if (pushEnabled) {
                Text(
                    text = "NOTIFICATION TYPES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                NotificationToggleItem(
                    title = "Habit Reminders",
                    subtitle = "Daily reminders for your habits",
                    checked = uiState.habitRemindersEnabled,
                    onCheckedChange = viewModel::toggleHabitReminders
                )

                NotificationToggleItem(
                    title = "Task Reminders",
                    subtitle = "Reminders for due tasks",
                    checked = uiState.taskRemindersEnabled,
                    onCheckedChange = viewModel::toggleTaskReminders
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NotificationToggleItem(
                    title = "Social Notifications",
                    subtitle = "Likes, comments, and new followers",
                    checked = uiState.socialNotificationsEnabled,
                    onCheckedChange = viewModel::toggleSocialNotifications
                )

                NotificationToggleItem(
                    title = "Challenge Updates",
                    subtitle = "Leaderboard changes and challenge milestones",
                    checked = uiState.challengeUpdatesEnabled,
                    onCheckedChange = viewModel::toggleChallengeUpdates
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NotificationToggleItem(
                    title = "Focus Mode Reminders",
                    subtitle = "Notifications during focus sessions",
                    checked = uiState.focusModeRemindersEnabled,
                    onCheckedChange = viewModel::toggleFocusModeReminders
                )

                Text(
                    text = "SUMMARIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                NotificationToggleItem(
                    title = "Daily Digest",
                    subtitle = "Summary of your daily progress",
                    checked = uiState.dailyDigestEnabled,
                    onCheckedChange = viewModel::toggleDailyDigest
                )

                NotificationToggleItem(
                    title = "Weekly Report",
                    subtitle = "Weekly achievement summary",
                    checked = uiState.weeklyReportEnabled,
                    onCheckedChange = viewModel::toggleWeeklyReport
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
