package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = AppTextStyles.BodySmall.copy(color = AppColors.TextMuted)) },
        placeholder = placeholder?.let {
            {
                Text(
                    it,
                    style = AppTextStyles.Body.copy(color = AppColors.TextMuted)
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    it,
                    contentDescription = null,
                    tint = AppColors.TextMuted
                )
            }
        },
        trailingIcon = trailingContent,
        isError = error != null,
        supportingText = (error ?: supportingText)?.let { text ->
            {
                Text(
                    text,
                    style = AppTextStyles.Caption.copy(color = if (error != null) AppColors.Cancelled else AppColors.TextMuted)
                )
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = AppTextStyles.Body.copy(color = AppColors.TextDark),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = AppShapes.Field,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Terracotta,
            unfocusedBorderColor = AppColors.Outline,
            disabledBorderColor = AppColors.OutlineSoft,
            errorBorderColor = AppColors.Cancelled,
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface,
            disabledContainerColor = AppColors.SurfaceMuted,
            errorContainerColor = AppColors.Surface,
            cursorColor = AppColors.Terracotta,
            focusedLabelColor = AppColors.Terracotta,
            disabledTextColor = AppColors.TextSecondary
        )
    )
}
