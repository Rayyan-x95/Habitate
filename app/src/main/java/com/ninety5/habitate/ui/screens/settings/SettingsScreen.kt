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
import androidx.compose.ui.res.stringResource
import com.ninety5.habitate.ui.components.BetaBadge
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.BuildConfig
import com.ninety5.habitate.R

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
    var hasObservedLoggedInState by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle navigation when logged out (either by logout or delete account)
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            hasObservedLoggedInState = true
        } else if (hasObservedLoggedInState) {
            onLogout()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_account)) },
            text = { 
                Text(stringResource(R.string.settings_delete_account_message)) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = HabitateTheme.colors.error)
                ) {
                    Text(stringResource(R.string.settings_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
            containerColor = HabitateTheme.colors.surface,
            titleContentColor = HabitateTheme.colors.onSurface,
            textContentColor = HabitateTheme.colors.onSurfaceVariant
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                title = stringResource(R.string.settings_edit_profile),
                onClick = onNavigateToEditProfile
            )
            HorizontalDivider()
            
            // Theme Settings
            Text(
                text = stringResource(R.string.settings_section_appearance),
                style = HabitateTheme.typography.labelLarge,
                color = HabitateTheme.colors.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            SettingsItem(
                icon = when(uiState.themeMode) {
                    "light" -> Icons.Rounded.LightMode
                    "dark" -> Icons.Rounded.DarkMode
                    else -> Icons.Rounded.SettingsSystemDaydream
                },
                title = stringResource(R.string.settings_theme, uiState.themeMode.replaceFirstChar { it.uppercase() }),
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
                title = stringResource(R.string.settings_health_connect),
                onClick = onNavigateToHealthConnect
            )
            HorizontalDivider()
            
            SettingsItem(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.settings_notifications),
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
                title = stringResource(R.string.settings_private_account),
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
                text = stringResource(R.string.settings_section_data),
                style = HabitateTheme.typography.labelLarge,
                color = HabitateTheme.colors.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Archive,
                title = stringResource(R.string.settings_archived_items),
                onClick = onNavigateToArchive
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Description,
                title = stringResource(R.string.settings_public_api),
                onClick = onNavigateToPublicApi
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Security,
                title = stringResource(R.string.settings_privacy_dashboard),
                onClick = onNavigateToPrivacyDashboard
            )
            HorizontalDivider()

            // Legal & Compliance
            Text(
                text = stringResource(R.string.settings_section_legal),
                style = HabitateTheme.typography.labelLarge,
                color = HabitateTheme.colors.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Policy,
                title = stringResource(R.string.settings_privacy_policy),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider()

            SettingsItem(
                icon = Icons.Rounded.Description,
                title = stringResource(R.string.settings_terms_of_service),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider()
            
            SettingsItem(
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                title = stringResource(R.string.settings_logout),
                onClick = { authViewModel.logout() },
                textColor = HabitateTheme.colors.error
            )
            HorizontalDivider()
            SettingsItem(
                icon = Icons.Rounded.DeleteForever,
                title = stringResource(R.string.settings_delete_account),
                onClick = { showDeleteDialog = true },
                textColor = HabitateTheme.colors.error
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
                        text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        style = HabitateTheme.typography.bodySmall,
                        color = HabitateTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BetaBadge(text = stringResource(R.string.settings_public_beta))
                }
                Text(
                    text = stringResource(R.string.settings_build, BuildConfig.VERSION_CODE),
                    style = HabitateTheme.typography.labelSmall,
                    color = HabitateTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
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
    textColor: androidx.compose.ui.graphics.Color = HabitateTheme.colors.onSurface,
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
