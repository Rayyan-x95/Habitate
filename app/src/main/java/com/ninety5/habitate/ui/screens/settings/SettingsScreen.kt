package com.ninety5.habitate.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsSystemDaydream
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.screens.auth.AuthViewModel

import androidx.compose.material3.Switch

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import com.ninety5.habitate.ui.components.BetaBadge
import com.ninety5.habitate.BuildConfig

private const val PRIVACY_POLICY_URL = "https://habitate.app/privacy"
private const val TERMS_OF_SERVICE_URL = "https://habitate.app/terms"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToPrivacyDashboard: () -> Unit,
    onNavigateToPublicApi: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle navigation when logged out (either by logout or delete account)
    LaunchedEffect(authState.isLoggedIn) {
        if (!authState.isLoggedIn) {
            onLogout()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { 
                Text("Are you sure you want to delete your account? This action is permanent and cannot be undone. All your data will be lost.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsItem(
                icon = Icons.Rounded.Edit,
                title = "Edit Profile",
                onClick = onNavigateToEditProfile
            )
            HorizontalDivider()
            
            // Theme Settings
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            SettingsItem(
                icon = when(uiState.themeMode) {
                    "light" -> Icons.Rounded.LightMode
                    "dark" -> Icons.Rounded.DarkMode
                    else -> Icons.Rounded.SettingsSystemDaydream
                },
                title = "Theme: ${uiState.themeMode.replaceFirstChar { it.uppercase() }}",
                onClick = {
                    val nextMode = when(uiState.themeMode) {
                        "system" -> "light"
                        "light" -> "dark"
                        else -> "system"
                    }
                    viewModel.setThemeMode(nextMode)
                }
            )
            
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Favorite,
                title = "Health Connect",
                onClick = onNavigateToHealthConnect
            )
            HorizontalDivider()
            
            SettingsItem(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                onClick = onNavigateToNotifications,
                trailingContent = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            )
            HorizontalDivider()
            
            SettingsItem(
                icon = Icons.Rounded.Security,
                title = "Private Account",
                onClick = { viewModel.togglePrivateAccount(!uiState.isPrivateAccount) },
                trailingContent = {
                    Switch(
                        checked = uiState.isPrivateAccount,
                        onCheckedChange = { viewModel.togglePrivateAccount(it) }
                    )
                }
            )
            HorizontalDivider()

            // Data & Storage
            Text(
                text = "Data & Storage",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Archive,
                title = "Archived Items",
                onClick = onNavigateToArchive
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Description,
                title = "Public APIs Integration",
                onClick = onNavigateToPublicApi
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Security,
                title = "Privacy Dashboard",
                onClick = onNavigateToPrivacyDashboard
            )
            HorizontalDivider()

            // Legal & Compliance
            Text(
                text = "Legal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Policy,
                title = "Privacy Policy",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Description,
                title = "Terms of Service",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider()
            
            SettingsItem(
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                title = "Logout",
                onClick = { authViewModel.logout() },
                textColor = MaterialTheme.colorScheme.error
            )
            HorizontalDivider()
            SettingsItem(
                icon = Icons.Rounded.DeleteForever,
                title = "Delete Account",
                onClick = { showDeleteDialog = true },
                textColor = MaterialTheme.colorScheme.error
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Version Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Habitate v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BetaBadge(text = "PUBLIC BETA")
                }
                Text(
                    text = "Build ${BuildConfig.VERSION_CODE}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title, color = textColor) },
        leadingContent = { Icon(icon, contentDescription = null, tint = textColor) },
        trailingContent = trailingContent,
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}
