package com.ninety5.habitate.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.InputShape

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - INPUTS                       ║
 * ║                         Version 3.0 - 2026 Spatial UI                    ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Filled tonal style (soft background, no hard outline)                 ║
 * ║  • Floating labels                                                       ║
 * ║  • Soft accent glow on focus                                             ║
 * ║  • 16dp corner radius                                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = InputShape // 16dp
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = HabitateTheme.colors.inputBackground,
                unfocusedContainerColor = HabitateTheme.colors.inputBackground,
                disabledContainerColor = HabitateTheme.colors.surfaceVariant,
                errorContainerColor = HabitateTheme.colors.errorContainer,
                
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                
                focusedTextColor = HabitateTheme.colors.textPrimary,
                unfocusedTextColor = HabitateTheme.colors.textPrimary,
                disabledTextColor = HabitateTheme.colors.textDisabled,
                errorTextColor = HabitateTheme.colors.textPrimary,
                
                focusedLabelColor = HabitateTheme.colors.primary,
                unfocusedLabelColor = HabitateTheme.colors.textSecondary,
                disabledLabelColor = HabitateTheme.colors.textDisabled,
                errorLabelColor = HabitateTheme.colors.error,
                
                focusedPlaceholderColor = HabitateTheme.colors.textMuted,
                unfocusedPlaceholderColor = HabitateTheme.colors.textMuted,
                disabledPlaceholderColor = HabitateTheme.colors.textDisabled,
                errorPlaceholderColor = HabitateTheme.colors.textMuted,
                
                focusedLeadingIconColor = HabitateTheme.colors.primary,
                unfocusedLeadingIconColor = HabitateTheme.colors.textSecondary,
                disabledLeadingIconColor = HabitateTheme.colors.textDisabled,
                errorLeadingIconColor = HabitateTheme.colors.error,
                
                focusedTrailingIconColor = HabitateTheme.colors.primary,
                unfocusedTrailingIconColor = HabitateTheme.colors.textSecondary,
                disabledTrailingIconColor = HabitateTheme.colors.textDisabled,
                errorTrailingIconColor = HabitateTheme.colors.error,
            )
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = HabitateTheme.colors.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
