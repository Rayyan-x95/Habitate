package com.ninety5.habitate.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.components.HabitateLogo
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSecondaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTextButton
import com.ninety5.habitate.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToVerifyEmail: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val colors = HabitateTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn, uiState.isOnboarded, uiState.isEmailVerified) {
        if (uiState.isLoggedIn) {
            if (!uiState.isEmailVerified) {
                onNavigateToVerifyEmail()
            } else if (uiState.isOnboarded) {
                onLoginSuccess()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GradientSurface)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GradientBrand),
                    contentAlignment = Alignment.Center
                ) {
                    HabitateLogo(
                        size = 48.dp,
                        tint = colors.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Welcome back",
                    style = ScreenTitle,
                    color = colors.textPrimary
                )

                Text(
                    text = "Sign in to continue your journey",
                    style = BodyText,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = Spacing.sm)
                )

                Spacer(modifier = Modifier.height(Spacing.xxxl))

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
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = colors.textMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )

                // Forgot password
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    HabitateTextButton(
                        text = "Forgot Password?",
                        onClick = onNavigateToForgotPassword
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Error message
                AnimatedVisibility(
                    visible = uiState.error != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = colors.errorContainer,
                        shape = CardShape,
                        modifier = Modifier.padding(bottom = Spacing.lg)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = colors.error,
                            modifier = Modifier.padding(Spacing.lg),
                            style = BodyText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Login button
                HabitatePrimaryButton(
                    text = "Sign In",
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    loading = uiState.isLoading,
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Email Link Login button
                if (uiState.isEmailLinkAvailable) {
                    HabitateSecondaryButton(
                        text = "Sign in with Email Link",
                        onClick = { 
                            if (email.isNotBlank()) {
                                viewModel.sendSignInLink(email) 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && email.isNotBlank()
                    )
                }

                // Email link sent success message
                AnimatedVisibility(
                    visible = uiState.emailLinkSent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = colors.successContainer,
                        shape = CardShape,
                        modifier = Modifier.padding(vertical = Spacing.lg)
                    ) {
                        Text(
                            text = "✓ Sign-in link sent to $email\nCheck your inbox!",
                            color = colors.success,
                            modifier = Modifier.padding(Spacing.lg),
                            style = BodyText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(Spacing.xl))

                // Register link
                Row(
                    modifier = Modifier.padding(bottom = Spacing.xl),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = BodyText,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Sign Up",
                        style = BodyText,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .padding(Spacing.xs)
                    )
                }
            }
        }
    }
}
