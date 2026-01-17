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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninety5.habitate.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Rounded.RocketLaunch,
            title = "Reach your goals with no distractions",
            description = "",
            accentColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            icon = Icons.Rounded.Diversity3,
            title = "Welcome to the community!",
            description = "",
            accentColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            icon = Icons.Rounded.Favorite,
            title = "A place where wellbeing combines with social aspects",
            description = "Together for a better us.",
            accentColor = MaterialTheme.colorScheme.tertiary
        ),
        OnboardingPage(
            icon = Icons.Rounded.TrendingUp,
            title = "Inspire yourself to do better",
            description = "",
            accentColor = MaterialTheme.colorScheme.primary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                // Page indicators
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.Start
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
                                .padding(end = 8.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Action button (Circular Arrow)
                IconButton(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = HabitateOffWhite,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Placeholder for Illustration
        // In a real app, use Image(painter = painterResource(id = page.imageRes), ...)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color.Transparent), // Transparent background for illustration
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = HabitateOffWhite
            )
        }

        if (page.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = page.description,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = HabitateOffWhite
            )
        }
    }
}

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)
