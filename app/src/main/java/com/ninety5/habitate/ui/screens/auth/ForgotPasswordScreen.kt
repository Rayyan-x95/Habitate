package com.ninety5.habitate.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.components.designsystem.HabitateBackButton
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTextButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTopBar
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val colors = HabitateTheme.colors

    var email by remember { mutableStateOf("") }

    LaunchedEffect(uiState.passwordResetSent) {
        if (uiState.passwordResetSent) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearPasswordResetSent()
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        HabitateTopBar(
            title = "",
            navigationIcon = { HabitateBackButton(onClick = onNavigateBack) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Icon
            Surface(
                shape = RoundedCornerShape(Radius.md),
                color = colors.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(Size.iconLg),
                        tint = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Reset Password",
                style = HabitateTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Text(
                text = "Enter your email address and we'll send you a link to reset your password",
                style = HabitateTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xl)
            )

            // Success message
            AnimatedVisibility(
                visible = uiState.passwordResetSent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = colors.primaryContainer,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier.padding(bottom = Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.primary
                        )
                        Text(
                            text = "Reset link sent! Check your inbox.",
                            color = colors.onPrimaryContainer,
                            style = HabitateTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Error message
            AnimatedVisibility(
                visible = uiState.error != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = colors.errorContainer,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier.padding(bottom = Spacing.md)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = colors.onErrorContainer,
                        modifier = Modifier.padding(Spacing.md),
                        style = HabitateTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = colors.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank()) {
                            viewModel.sendPasswordReset(email)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.borderSubtle
                ),
                enabled = !uiState.isLoading && !uiState.passwordResetSent
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Reset button
            HabitatePrimaryButton(
                text = "Send Reset Link",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.sendPasswordReset(email)
                },
                modifier = Modifier.fillMaxWidth(),
                loading = uiState.isLoading,
                enabled = !uiState.isLoading && email.isNotBlank() && !uiState.passwordResetSent
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            HabitateTextButton(
                text = "Back to Sign In",
                onClick = onNavigateBack,
                enabled = !uiState.isLoading
            )
        }
    }
}
