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
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
    this
        .background(backgroundColor, RoundedCornerShape(16.dp))
        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
}
