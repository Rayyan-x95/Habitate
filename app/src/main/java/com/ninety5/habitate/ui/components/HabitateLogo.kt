package com.ninety5.habitate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.R

/**
 * Standardized Habitate Logo Component.
 * Uses the stylized "H" logo from the app icon.
 */
@Composable
fun HabitateLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null
) {
    Image(
        painter = painterResource(id = R.drawable.ic_habitate_logo),
        contentDescription = "Habitate Logo",
        modifier = modifier.size(size),
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}
