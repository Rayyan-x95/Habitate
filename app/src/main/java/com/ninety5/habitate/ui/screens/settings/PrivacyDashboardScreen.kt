package com.ninety5.habitate.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ninety5.habitate.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDashboardScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete All Data?") },
            text = { 
                Text("This will permanently delete all your data including posts, habits, workouts, and journal entries. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
            title = { Text("Export Your Data") },
            text = { 
                Text("Your data will be exported as a JSON file. This includes your posts, habits, workouts, and journal entries.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.exportUserData()
                        showExportDialog = false
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Stealth Mode") },
                supportingContent = { Text("Hide your online status and activity") },
                leadingContent = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = uiState.isStealthMode,
                        onCheckedChange = { viewModel.toggleStealthMode(it) }
                    )
                }
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Visibility,
                title = "View As...",
                onClick = { navController.navigate("profile_view_as") }
            )
            HorizontalDivider()
            SettingsItem(
                icon = Icons.Rounded.Download,
                title = "Export My Data",
                onClick = { showExportDialog = true }
            )
            HorizontalDivider()
            SettingsItem(
                icon = Icons.Rounded.Delete,
                title = "Delete All Data",
                onClick = { showDeleteDialog = true },
                textColor = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text(
                text = "Habitate is designed with privacy first. Your health data is stored locally and only synced when you choose.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
