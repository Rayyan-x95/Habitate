package com.ninety5.habitate.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

fun Modifier.glassEffect() = composed {
    // Minimal style: Solid surface color, no blur or transparency
    val backgroundColor = MaterialTheme.colorScheme.surface
    
    this
        .background(backgroundColor, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
}
