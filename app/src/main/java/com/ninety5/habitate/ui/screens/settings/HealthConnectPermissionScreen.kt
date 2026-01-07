package com.ninety5.habitate.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.theme.SoftIndigo
import androidx.health.connect.client.PermissionController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectPermissionScreen(
    onBackClick: () -> Unit,
    viewModel: HealthConnectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()

    // Create the permission launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.checkStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Connect") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = \"Connect Health Data\",
                modifier = Modifier.size(64.dp),
                tint = SoftIndigo
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Connect Health Data",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Habitate can import your workouts and step counts. " +
                        "Your health data is never shared without your permission.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            when (uiState) {
                is HealthUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HealthUiState.NotAvailable -> {
                    Text(
                        "Health Connect is not available on this device.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is HealthUiState.NeedsPermission -> {
                    Button(
                        onClick = {
                            permissionLauncher.launch(viewModel.getPermissions())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo)
                    ) {
                        Text("Grant Health Permissions")
                    }
                }
                is HealthUiState.Ready -> {
                    Text(
                        "Health Connect is connected!",
                        color = SoftIndigo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.importWorkouts() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Recent Workouts")
                    }
                    importStatus?.let { status ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
