package com.ninety5.habitate.ui.screens.focus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.R
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSecondaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTonalButton
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitateTheme.colors.background)
    ) {
        AnimatedVisibility(
            visible = uiState.isSessionComplete,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            SessionCompleteScreen(onReset = { viewModel.resetSession() })
        }

        AnimatedVisibility(
            visible = !uiState.isSessionComplete,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FocusTimerScreen(
                uiState = uiState,
                onToggleTimer = { viewModel.toggleTimer() },
                onStopSession = { viewModel.stopSession() },
                onSetDuration = { viewModel.setDuration(it) },
                onPlaySound = { name, resId -> viewModel.playSound(name, resId) }
            )
        }
    }
}

@Composable
fun FocusTimerScreen(
    uiState: FocusUiState,
    onToggleTimer: () -> Unit,
    onStopSession: () -> Unit,
    onSetDuration: (Int) -> Unit,
    onPlaySound: (String, Int) -> Unit
) {
    val colors = HabitateTheme.colors
    val progress by animateFloatAsState(
        targetValue = if (uiState.initialDuration > 0) {
            uiState.timeLeftSeconds.toFloat() / uiState.initialDuration.toFloat()
        } else 0f,
        animationSpec = tween(500),
        label = "timer_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Timer ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(240.dp),
                color = colors.surfaceVariant.copy(alpha = 0.3f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            // Progress arc
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(240.dp),
                color = colors.primary,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            // Time display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(uiState.timeLeftSeconds),
                    style = HabitateTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    ),
                    color = colors.onBackground
                )
                if (uiState.isTimerRunning) {
                    Text(
                        text = stringResource(R.string.focus_focusing_label),
                        style = HabitateTheme.typography.labelMedium,
                        color = colors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))

        // Play/Pause + Stop buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitatePrimaryButton(
                text = if (uiState.isTimerRunning) stringResource(R.string.focus_pause_button) else stringResource(R.string.focus_start_button),
                onClick = onToggleTimer,
                icon = if (uiState.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                modifier = Modifier.width(160.dp)
            )

            AnimatedVisibility(
                visible = uiState.isTimerRunning || uiState.timeLeftSeconds != uiState.initialDuration
            ) {
                HabitateSecondaryButton(
                    text = stringResource(R.string.focus_stop_button),
                    onClick = onStopSession,
                    icon = Icons.Default.Stop
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Duration selector (only when timer not running)
        AnimatedVisibility(visible = !uiState.isTimerRunning) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.focus_duration_label),
                    style = HabitateTheme.typography.titleSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    listOf(15, 25, 50).forEach { minutes ->
                        SuggestionChip(
                            onClick = { onSetDuration(minutes) },
                            label = {
                                Text(
                                    "${minutes}m",
                                    style = HabitateTheme.typography.labelLarge,
                                    fontWeight = if (uiState.initialDuration == minutes * 60L) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (uiState.initialDuration == minutes * 60L)
                                    colors.primary.copy(alpha = 0.12f)
                                else
                                    colors.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = if (uiState.initialDuration == minutes * 60L)
                                    colors.primary
                                else
                                    colors.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(Radius.md)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Ambient Sound
        Text(
            text = stringResource(R.string.focus_ambient_sound_label),
            style = HabitateTheme.typography.titleSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FilterChip(
                selected = uiState.selectedSound == "Rain",
                onClick = { onPlaySound("Rain", R.raw.rain) },
                label = { Text(stringResource(R.string.focus_sound_rain)) },
                leadingIcon = { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.12f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = colors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            FilterChip(
                selected = uiState.selectedSound == "Forest",
                onClick = { onPlaySound("Forest", R.raw.forest) },
                label = { Text(stringResource(R.string.focus_sound_forest)) },
                leadingIcon = { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.12f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = colors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(Radius.md)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Spotify
        Text(
            text = stringResource(R.string.focus_music_label),
            style = HabitateTheme.typography.titleSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        val context = LocalContext.current
        HabitateTonalButton(
            text = stringResource(R.string.focus_open_spotify),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:genre:focus"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/genre/focus"))
                    webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(webIntent)
                    } catch (_: ActivityNotFoundException) {
                        android.widget.Toast.makeText(context, context.getString(R.string.focus_no_browser), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            icon = Icons.Default.MusicNote
        )
    }
}

@Composable
fun SessionCompleteScreen(onReset: () -> Unit) {
    val colors = HabitateTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.focus_session_completed_desc),
                tint = colors.primary,
                modifier = Modifier.size(Size.iconXl)
            )
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = stringResource(R.string.focus_session_complete_title),
            style = HabitateTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = stringResource(R.string.focus_great_job),
            style = HabitateTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.xxl))
        HabitatePrimaryButton(
            text = stringResource(R.string.focus_start_new_session),
            onClick = onReset
        )
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
