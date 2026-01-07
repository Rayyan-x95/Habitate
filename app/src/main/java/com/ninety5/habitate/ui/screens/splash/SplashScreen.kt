package com.ninety5.habitate.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                      HABITATE SPLASH SCREEN (PIXEL-PERFECT)               ║
 * ║                                                                          ║
 * ║  Replicates the splash screen from reference images:                     ║
 * ║  • Dark green gradient background                                         ║
 * ║  • Large "H" logo in center                                              ║
 * ║  • "Habitate" text below logo                                            ║
 * ║  • Smooth fade-in animation                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    viewModel: SplashViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // Animation states
    val alphaState = remember { androidx.compose.animation.core.Animatable(0f) }
    val scaleState = remember { androidx.compose.animation.core.Animatable(0.8f) }
    
    // Start animations
    LaunchedEffect(Unit) {
        // Fade in and scale up simultaneously
        alphaState.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutCubic
            )
        )
        
        scaleState.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = EaseOutBack
            )
        )
        
        // Wait for splash duration
        kotlinx.coroutines.delay(2000)
        
        // Fade out
        alphaState.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = EaseInCubic
            )
        )
        
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = PrimaryGradient
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // H Logo
            Box(
                modifier = Modifier
                    .size(120.dp) // Large logo as seen in reference
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "H",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.scale(scaleState.value)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Habitate Text
            Text(
                text = "Habitate",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = 2.sp, // Slight letter spacing for elegance
                modifier = Modifier.scale(scaleState.value)
            )
        }
    }
}

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    LOADING SCREEN (PIXEL-PERFECT)                        ║
 * ║                                                                          ║
 * ║  Secondary loading screen with abstract shapes:                         ║
 * ║  • Same dark green gradient background                                   ║
 * ║  • Abstract white shapes floating                                       ║
 * ║  • Subtle animation                                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")
    
    // Floating animation for shapes
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )
    
    // Rotation animation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = PrimaryGradient
            )
    ) {
        // Abstract Shape 1 - Large circle
        Box(
            modifier = Modifier
                .offset(
                    x = (-100 + floatOffset * 50).dp,
                    y = (-150 + floatOffset * 30).dp
                )
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.05f),
                    CircleShape
                )
        )
        
        // Abstract Shape 2 - Medium circle
        Box(
            modifier = Modifier
                .offset(
                    x = (150 + floatOffset * -40).dp,
                    y = (100 + floatOffset * 20).dp
                )
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.03f),
                    CircleShape
                )
        )
        
        // Abstract Shape 3 - Small circle
        Box(
            modifier = Modifier
                .offset(
                    x = (50 + floatOffset * 30).dp,
                    y = (-200 + floatOffset * 40).dp
                )
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.04f),
                    CircleShape
                )
        )
        
        // Center loading indicator
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = "Loading...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
