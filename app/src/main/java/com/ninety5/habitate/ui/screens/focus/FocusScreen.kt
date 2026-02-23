package com.ninety5.habitate.ui.screens.focus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.CheckCircle
import com.ninety5.habitate.R
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSessionComplete) {
        SessionCompleteScreen(onReset = { viewModel.resetSession() })
    } else {
        FocusTimerScreen(
            uiState = uiState,
            onToggleTimer = { viewModel.toggleTimer() },
            onStopSession = { viewModel.stopSession() },
            onSetDuration = { viewModel.setDuration(it) },
            onPlaySound = { name, resId -> viewModel.playSound(name, resId) }
        )
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
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatTime(uiState.timeLeftSeconds),
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onToggleTimer) {
                Icon(
                    imageVector = if (uiState.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isTimerRunning) "Pause" else "Start"
                )
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.isTimerRunning) "Pause" else "Start")
            }
            
            if (uiState.isTimerRunning || uiState.timeLeftSeconds != uiState.initialDuration) {
                OutlinedButton(onClick = onStopSession) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!uiState.isTimerRunning) {
            Text("Duration", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = { onSetDuration(25) },
                    label = { Text("25m") }
                )
                SuggestionChip(
                    onClick = { onSetDuration(50) },
                    label = { Text("50m") }
                )
                SuggestionChip(
                    onClick = { onSetDuration(15) },
                    label = { Text("15m") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Ambient Sound", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.selectedSound == "Rain",
                onClick = { onPlaySound("Rain", R.raw.rain) },
                label = { Text("Rain") },
                leadingIcon = { Icon(Icons.Default.MusicNote, null) }
            )
            FilterChip(
                selected = uiState.selectedSound == "Forest",
                onClick = { onPlaySound("Forest", R.raw.forest) },
                label = { Text("Forest") },
                leadingIcon = { Icon(Icons.Default.MusicNote, null) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Music", style = MaterialTheme.typography.titleMedium)
        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:genre:focus"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to web if app not installed
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/genre/focus"))
                    context.startActivity(webIntent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_spotify), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Open Spotify Focus")
        }
    }
}

@Composable
fun SessionCompleteScreen(onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Session Complete!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Great job staying focused.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReset) {
            Text("Start New Session")
        }
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
