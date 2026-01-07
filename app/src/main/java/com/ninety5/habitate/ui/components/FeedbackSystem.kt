package com.ninety5.habitate.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSecondaryButton
import com.ninety5.habitate.ui.theme.*
import kotlinx.coroutines.delay

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                      SUCCESS & FEEDBACK SYSTEM                             ║
 * ║                                                                          ║
 * ║  Enhanced feedback states for better UX:                                  ║
 * ║  • Success toast/snackbar notifications                                   ║
 * ║  • Error states with actionable messages                                  ║
 * ║  • Loading states with progress indicators                              ║
 * ║  • Progress feedback for multi-step processes                            ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Success toast notification
 */
@Composable
fun SuccessToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseOutBack
        ),
        label = "success_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(RefRadiusSM.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50) // Success green
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RefSpacingMD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(RefIconSizeSM.dp)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            Text(
                text = message,
                color = Color.White,
                fontSize = RefTextSizeMD.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(RefIconSizeSM.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(RefIconSizeXS.dp)
                )
            }
        }
    }
}

/**
 * Error state with actionable guidance
 */
@Composable
fun ErrorState(
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RefSpacingXL.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon with pulse animation
        val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "error_scale"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(RefRadiusFull.dp))
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
            )
        }
        
        Spacer(modifier = Modifier.height(RefSpacingLG.dp))
        
        Text(
            text = title,
            fontSize = RefTextSizeXL.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(RefSpacingMD.dp))
        
        Text(
            text = message,
            fontSize = RefTextSizeMD.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = RefLineHeightBody.sp
        )
        
        // Action buttons
        if (onRetryClick != null || onActionClick != null) {
            Spacer(modifier = Modifier.height(RefSpacingXL.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(RefSpacingMD.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRetryClick != null) {
                    HabitateSecondaryButton(
                        text = "Retry",
                        onClick = onRetryClick
                    )
                }
                
                if (onActionClick != null && actionText != null) {
                    HabitatePrimaryButton(
                        text = actionText,
                        onClick = onActionClick
                    )
                }
            }
        }
    }
}

/**
 * Loading state with progress indicator
 */
@Composable
fun LoadingState(
    message: String = "Loading...",
    showProgress: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RefSpacingXL.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(RefIconSizeXXL.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(RefSpacingLG.dp))
        
        Text(
            text = message,
            fontSize = RefTextSizeMD.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (showProgress) {
            Spacer(modifier = Modifier.height(RefSpacingMD.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(RefRadiusSM.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(RefSpacingXS.dp))
            
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = RefTextSizeSM.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Progress feedback for multi-step processes
 */
@Composable
fun ProgressFeedback(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(RefSpacingMD.dp))
        
        // Step indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Step circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < currentStep -> ReferenceColors.accent
                                    index == currentStep -> ReferenceColors.accent
                                    else -> ReferenceColors.border
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (index == currentStep) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(RefSpacingXS.dp))
                    
                    // Step title
                    Text(
                        text = stepTitles.getOrNull(index) ?: "Step ${index + 1}",
                        fontSize = RefTextSizeXS.sp,
                        color = if (index <= currentStep) ReferenceColors.textPrimary else ReferenceColors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Info toast for general notifications
 */
@Composable
fun InfoToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RefRadiusSM.dp)),
        colors = CardDefaults.cardColors(
            containerColor = ReferenceColors.accent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RefSpacingMD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.White,
                modifier = Modifier.size(RefIconSizeSM.dp)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            Text(
                text = message,
                color = Color.White,
                fontSize = RefTextSizeMD.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(RefIconSizeSM.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(RefIconSizeXS.dp)
                )
            }
        }
    }
}

/**
 * Warning toast for caution messages
 */
@Composable
fun WarningToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RefRadiusSM.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800) // Warning orange
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RefSpacingMD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.White,
                modifier = Modifier.size(RefIconSizeSM.dp)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            Text(
                text = message,
                color = Color.White,
                fontSize = RefTextSizeMD.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(RefSpacingSM.dp))
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(RefIconSizeSM.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(RefIconSizeXS.dp)
                )
            }
        }
    }
}
