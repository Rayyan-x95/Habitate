package com.ninety5.habitate.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Spacing

/**
 * Welcome screen with calm, premium design following the Habitate design system.
 * Uses logo-derived colors and subtle animations.
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val colors = HabitateTheme.colors

    // Animation states
    val logoScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val buttonsOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        contentAlpha.animateTo(1f, animationSpec = tween(300))
        buttonsOffset.animateTo(0f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.25f))

            // Logo
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                HabitateLogo(
                    size = 72.dp,
                    tint = colors.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                text = "Habitate",
                style = HabitateTheme.typography.displayMedium,
                color = colors.onBackground,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "Build habits. Track progress.\nGrow together.",
                style = HabitateTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.weight(0.35f))

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha.value)
                    .graphicsLayer { translationY = buttonsOffset.value },
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                HabitatePrimaryButton(
                    text = "Get Started",
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth()
                )

                HabitateSecondaryButton(
                    text = "I already have an account",
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                style = HabitateTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(contentAlpha.value * 0.8f)
                    .padding(bottom = Spacing.lg)
            )
        }
    }
}
