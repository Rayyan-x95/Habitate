package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
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
 * ║  Clean, accessible, calm input components                                ║
 * ║  Design principle: Clear focus states, helpful feedback, minimal borders ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// TEXT FIELD (Primary input component)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard text field with label, helper text, and clear focus states.
 * Calm aesthetic with subtle borders and smooth focus transitions.
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
    successText: String? = null,
    isError: Boolean = errorText != null,
    isSuccess: Boolean = successText != null,
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
    
    val labelColor = when {
        isError -> colors.error
        isSuccess -> colors.success
        else -> colors.textSecondary
    }
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MetaText,
                color = labelColor
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
                // Focus states
                focusedBorderColor = if (isSuccess) colors.success else colors.inputBorderFocused,
                unfocusedBorderColor = if (isSuccess) colors.success else colors.borderSubtle,
                errorBorderColor = colors.error.copy(alpha = 0.8f),
                // Container colors
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.inputBackground,
                errorContainerColor = colors.errorContainer.copy(alpha = 0.08f),
                // Text colors
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                // Cursor and indicator
                cursorColor = colors.primary,
                focusedLabelColor = colors.primary,
                // Disabled states
                disabledBorderColor = colors.border.copy(alpha = 0.4f),
                disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                disabledTextColor = colors.textDisabled
            )
        )
        
        // Helper/Error/Success text
        if (errorText != null || helperText != null || successText != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = errorText ?: successText ?: helperText ?: "",
                style = CaptionText,
                color = when {
                    errorText != null -> colors.error
                    successText != null -> colors.success
                    else -> colors.textMuted
                }
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
// SEARCH FIELD (Prominent, pill-shaped)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Search input with icon and subtle focus state.
 * Pill-shaped for a friendly, approachable feel.
 */
@Composable
fun HabitateSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val colors = HabitateTheme.colors
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor = if (isFocused) colors.inputBorderFocused else colors.borderSubtle
    val backgroundColor = if (isFocused) colors.surface else colors.inputBackground
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Size.inputHeightSmall)
            .clip(SearchBarShape)
            .background(backgroundColor)
            .border(
                width = if (isFocused) 1.5.dp else Size.borderThin,
                color = borderColor,
                shape = SearchBarShape
            )
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            modifier = Modifier.size(Size.iconMd),
            tint = if (isFocused) colors.primary else colors.textMuted
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
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
        
        // Clear button when there's text
        if (value.isNotEmpty() && onClear != null) {
            Spacer(Modifier.width(Spacing.xs))
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(Size.iconMd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(Size.iconSm),
                    tint = colors.textMuted
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TEXT AREA (Multi-line input)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Multi-line text area for longer content with character counter.
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
    maxLength: Int? = null,
    showCharCount: Boolean = maxLength != null
) {
    val colors = HabitateTheme.colors
    val isError = errorText != null
    val isOverLimit = maxLength != null && value.length > maxLength
    
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
                // Allow typing but show error if over limit
                onValueChange(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            enabled = enabled,
            placeholder = placeholder?.let {
                { Text(text = it, style = BodyText, color = colors.textMuted) }
            },
            isError = isError || isOverLimit,
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            shape = InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isOverLimit) colors.error else colors.inputBorderFocused,
                unfocusedBorderColor = colors.borderSubtle,
                errorBorderColor = colors.error.copy(alpha = 0.8f),
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.inputBackground,
                cursorColor = colors.primary
            )
        )
        
        Spacer(Modifier.height(Spacing.xs))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Error or helper text
            Text(
                text = errorText ?: helperText ?: "",
                style = CaptionText,
                color = if (isError) colors.error else colors.textMuted,
                modifier = Modifier.weight(1f, fill = false)
            )
            
            // Character counter
            if (showCharCount && maxLength != null) {
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "${value.length}/$maxLength",
                    style = CaptionText,
                    color = when {
                        isOverLimit -> colors.error
                        value.length > maxLength * 0.9 -> colors.warning
                        else -> colors.textMuted
                    }
                )
            }
        }
    }
}
