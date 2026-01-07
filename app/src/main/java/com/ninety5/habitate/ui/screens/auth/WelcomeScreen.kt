package com.ninety5.habitate.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.components.HabitateLogo
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSecondaryButton
import com.ninety5.habitate.ui.theme.*

/**
 * Welcome screen with calm, premium design following the Habitate design system.
 * Uses logo-derived colors and subtle animations.
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // Animation states
    val logoScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val buttonsOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        // Gentle logo animation - no bouncy overshoot
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Fade in content
        contentAlpha.animateTo(1f, animationSpec = tween(Duration.medium))
        buttonsOffset.animateTo(0f, animationSpec = tween(Duration.medium))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Minimal background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.25f))

            // Logo container with brand gradient
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                HabitateLogo(
                    size = 72.dp,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            // App name
            Text(
                text = "Habitate",
                style = Typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Tagline - calm, not aggressive
            Text(
                text = "Build habits. Track progress.\nGrow together.",
                style = BodyText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.weight(0.35f))

            // Buttons section with stagger animation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha.value)
                    .graphicsLayer { translationY = buttonsOffset.value },
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Primary CTA
                HabitatePrimaryButton(
                    text = "Get Started",
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary action
                HabitateSecondaryButton(
                    text = "I already have an account",
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            // Terms and privacy - subtle, not intrusive
            Text(
                text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                style = CaptionText,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(contentAlpha.value * 0.8f)
                    .padding(bottom = Spacing.lg)
            )
        }
    }
}
