package com.ninety5.habitate.ui.screens.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTextButton
import com.ninety5.habitate.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val colors = HabitateTheme.colors
    
    // Using brand-derived colors for each page
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Rounded.CheckCircle,
            title = "Build Better Habits",
            description = "Track your daily habits with streaks, reminders, and smart insights. Small steps lead to big changes.",
            accentColor = colors.primary
        ),
        OnboardingPage(
            icon = Icons.Rounded.FitnessCenter,
            title = "Track Your Fitness",
            description = "Log workouts, sync with Health Connect, and watch your progress grow over time.",
            accentColor = colors.success
        ),
        OnboardingPage(
            icon = Icons.Rounded.Groups,
            title = "Join Communities",
            description = "Connect with friends in Habitats. Share achievements, compete in challenges, and stay motivated together.",
            accentColor = colors.accent
        ),
        OnboardingPage(
            icon = Icons.Rounded.Timeline,
            title = "Your Life Timeline",
            description = "Everything you do is saved to your private timeline. Reflect on your journey and celebrate milestones.",
            accentColor = Primary400
        ),
        OnboardingPage(
            icon = Icons.Rounded.Psychology,
            title = "AI-Powered Insights",
            description = "Get personalized recommendations based on your patterns. Understand yourself better with smart analytics.",
            accentColor = colors.info
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.End
            ) {
                HabitateTextButton(
                    text = "Skip",
                    onClick = onOnboardingComplete
                )
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(
                    page = pages[pageIndex],
                    isActive = pagerState.currentPage == pageIndex
                )
            }

            // Bottom section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.screenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators - using smooth spring animation
                Row(
                    modifier = Modifier.padding(bottom = Spacing.xl),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "indicator_width"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) colors.primary
                                    else colors.border
                                )
                        )
                    }
                }

                // Action button
                HabitatePrimaryButton(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Continue",
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Page counter
                Text(
                    text = "${pagerState.currentPage + 1} of ${pages.size}",
                    style = CaptionText,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.lg)
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean
) {
    val colors = HabitateTheme.colors
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Subtle layered icon container
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(page.accentColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(page.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = page.accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        Text(
            text = page.title,
            style = ScreenTitle,
            textAlign = TextAlign.Center,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = page.description,
            style = BodyText,
            textAlign = TextAlign.Center,
            color = colors.textSecondary
        )
    }
}

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)
