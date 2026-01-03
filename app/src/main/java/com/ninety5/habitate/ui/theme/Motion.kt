package com.ninety5.habitate.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - MOTION                       ║
 * ║                                                                          ║
 * ║  Motion Principles:                                                      ║
 * ║  • Purposeful - animations guide, not distract                          ║
 * ║  • Short - 150-300ms for most interactions                              ║
 * ║  • Calm - gentle easing, no bouncy overshoot                            ║
 * ║  • Respectful - honors Reduce Motion preference                          ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// DURATION TOKENS
// ═══════════════════════════════════════════════════════════════════════════

object Duration {
    /** 100ms - Micro interactions (hover, focus) */
    const val instant: Int = 100
    
    /** 150ms - Quick feedback (button press) */
    const val fast: Int = 150
    
    /** 200ms - Standard transitions */
    const val normal: Int = 200
    
    /** 300ms - Complex transitions (screen changes) */
    const val medium: Int = 300
    
    /** 400ms - Emphasis animations */
    const val slow: Int = 400
    
    /** 500ms - Enter/exit animations */
    const val slower: Int = 500
    
    /** 1000ms - Shimmer cycle */
    const val shimmer: Int = 1000
    
    /** Stagger delay for list items */
    const val staggerDelay: Long = 50L
}

// ═══════════════════════════════════════════════════════════════════════════
// EASING CURVES
// ═══════════════════════════════════════════════════════════════════════════

object Easing {
    /** Standard easing for most animations */
    val standard = FastOutSlowInEasing
    
    /** Linear for progress indicators */
    val linear = LinearEasing
    
    /** Gentle spring for natural feel */
    val gentle: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /** Responsive spring for feedback */
    val responsive: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /** Soft spring for subtle motion */
    val soft: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// ANIMATION SPECS
// ═══════════════════════════════════════════════════════════════════════════

object Motion {
    /** Button press scale animation */
    fun buttonPress(): AnimationSpec<Float> = tween(
        durationMillis = Duration.fast,
        easing = FastOutSlowInEasing
    )
    
    /** Card hover/press animation */
    fun cardPress(): AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    /** Screen transition fade */
    fun screenFade(): AnimationSpec<Float> = tween(
        durationMillis = Duration.medium,
        easing = FastOutSlowInEasing
    )
    
    /** List item stagger fade-in */
    fun listItemEnter(): AnimationSpec<Float> = tween(
        durationMillis = Duration.normal,
        easing = FastOutSlowInEasing
    )
    
    /** Success pulse */
    fun successPulse(): AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /** Shimmer animation */
    fun shimmer(): AnimationSpec<Float> = tween(
        durationMillis = Duration.shimmer,
        easing = LinearEasing
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// PRESS SCALE MODIFIER (Button press: 0.96 → 1.0)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Applies a subtle scale-down effect when pressed.
 * Scale: 1.0 → 0.96 → 1.0
 */
fun Modifier.pressScale(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) pressedScale else 1f,
            animationSpec = Motion.buttonPress()
        )
    }
    
    this
        .scale(scale.value)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {}
        )
}

/**
 * Simplified press scale with click handler
 */
fun Modifier.pressableScale(
    onClick: () -> Unit,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this.clickable(enabled = false, onClick = {})
    
    var isPressed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) pressedScale else 1f,
            animationSpec = Motion.buttonPress()
        )
    }
    
    this
        .scale(scale.value)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick() }
            )
        }
}

// ═══════════════════════════════════════════════════════════════════════════
// STAGGERED FADE-IN FOR LISTS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Applies staggered fade-in animation for list items.
 * @param index Item index in the list
 * @param delayPerItem Delay between each item (default 50ms)
 */
fun Modifier.staggeredFadeIn(
    index: Int,
    delayPerItem: Long = Duration.staggerDelay,
    durationMillis: Int = Duration.normal
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(16f) }
    
    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        )
    }
    
    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        )
    }
    
    this
        .alpha(alpha.value)
        .graphicsLayer { translationY = offsetY.value }
}

// ═══════════════════════════════════════════════════════════════════════════
// SHIMMER LOADING EFFECT
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Creates a shimmer effect for skeleton loading states.
 * Uses logo gradient colors for brand consistency.
 */
@Composable
fun rememberShimmerAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Duration.shimmer,
                easing = LinearEasing
            )
        ),
        label = "shimmerProgress"
    )
    return shimmerProgress
}

// ═══════════════════════════════════════════════════════════════════════════
// SUCCESS PULSE ANIMATION
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Subtle pulse animation for success states (likes, streaks, etc.)
 * Uses accent color from logo palette.
 */
fun Modifier.successPulse(
    trigger: Boolean
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            scale.animateTo(1.15f, animationSpec = Motion.successPulse())
            scale.animateTo(1f, animationSpec = Motion.successPulse())
        }
    }
    
    this.scale(scale.value)
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN TRANSITION HELPERS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Fade + slight vertical motion for screen transitions.
 */
fun Modifier.screenEnterTransition(): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(24f) }
    
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = Motion.screenFade())
    }
    
    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, animationSpec = Easing.gentle)
    }
    
    this
        .alpha(alpha.value)
        .graphicsLayer { translationY = offsetY.value }
}

// ═══════════════════════════════════════════════════════════════════════════
// REDUCE MOTION RESPECT
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Checks if reduced motion is preferred.
 * Animations should be instant or disabled when this returns true.
 */
// Note: In actual implementation, use LocalReducedMotion composition local
// or check AccessibilityManager.isReduceMotionEnabled
