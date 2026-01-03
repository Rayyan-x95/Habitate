package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - INPUTS                   ║
 * ║                                                                          ║
 * ║  Clean, accessible input components                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// TEXT FIELD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard text field with label and helper text.
 */
@Composable
fun HabitateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    val colors = HabitateTheme.colors
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MetaText,
                color = if (isError) colors.error else colors.textSecondary
            )
            Spacer(Modifier.height(Spacing.sm))
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            placeholder = placeholder?.let {
                { Text(text = it, style = BodyText, color = colors.textMuted) }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(Size.iconMd),
                        tint = colors.textMuted
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                errorBorderColor = colors.error,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.inputBackground,
                errorContainerColor = colors.errorContainer.copy(alpha = 0.1f)
            )
        )
        
        if (errorText != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = errorText,
                style = CaptionText,
                color = colors.error
            )
        } else if (helperText != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = helperText,
                style = CaptionText,
                color = colors.textMuted
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// PASSWORD FIELD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Password field with visibility toggle.
 */
@Composable
fun HabitatePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Password",
    placeholder: String? = "Enter your password",
    errorText: String? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = HabitateTheme.colors
    
    HabitateTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        errorText = errorText,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) 
                        Icons.Outlined.VisibilityOff 
                    else 
                        Icons.Outlined.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = colors.textMuted
                )
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// SEARCH FIELD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Search input with icon and clear button.
 */
@Composable
fun HabitateSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: ((String) -> Unit)? = null,
    enabled: Boolean = true
) {
    val colors = HabitateTheme.colors
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor = if (isFocused) colors.primary else colors.border
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Size.inputHeightSmall)
            .clip(RoundedCornerShape(Radius.pill))
            .background(colors.inputBackground)
            .border(
                width = Size.borderThin,
                color = borderColor,
                shape = RoundedCornerShape(Radius.pill)
            )
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            modifier = Modifier.size(Size.iconMd),
            tint = colors.textMuted
        )
        
        Spacer(Modifier.width(Spacing.sm))
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            textStyle = BodyText.copy(color = colors.textPrimary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch?.invoke(value) }
            ),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BodyText,
                            color = colors.textMuted
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TEXT AREA
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Multi-line text area for longer content.
 */
@Composable
fun HabitateTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 6,
    maxLength: Int? = null
) {
    val colors = HabitateTheme.colors
    val isError = errorText != null
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MetaText,
                color = if (isError) colors.error else colors.textSecondary
            )
            Spacer(Modifier.height(Spacing.sm))
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            enabled = enabled,
            placeholder = placeholder?.let {
                { Text(text = it, style = BodyText, color = colors.textMuted) }
            },
            isError = isError,
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            shape = InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                errorBorderColor = colors.error,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.inputBackground
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = CaptionText,
                    color = colors.error
                )
            } else if (helperText != null) {
                Text(
                    text = helperText,
                    style = CaptionText,
                    color = colors.textMuted
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            
            if (maxLength != null) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = CaptionText,
                    color = if (value.length >= maxLength) colors.error else colors.textMuted
                )
            }
        }
    }
}
