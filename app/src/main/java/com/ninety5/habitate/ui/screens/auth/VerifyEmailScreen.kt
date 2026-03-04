package com.ninety5.habitate.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSecondaryButton
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Spacing

@Composable
fun VerifyEmailScreen(
    onEmailVerified: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = HabitateTheme.colors

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkEmailVerification()
    }

    LaunchedEffect(uiState.isEmailVerified) {
        if (uiState.isEmailVerified) {
            onEmailVerified()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = "Verify your email",
            style = HabitateTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colors.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "We've sent a verification email to your address. Please check your inbox and click the link to continue.",
            style = HabitateTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        HabitatePrimaryButton(
            text = "I've Verified My Email",
            onClick = { viewModel.checkEmailVerification() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        HabitateSecondaryButton(
            text = "Resend Verification Email",
            onClick = { viewModel.sendVerificationEmail() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        if (uiState.verificationEmailSent) {
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = "Verification email sent!",
                color = colors.primary,
                style = HabitateTheme.typography.bodyMedium
            )
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = uiState.error ?: "",
                color = colors.error,
                style = HabitateTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
