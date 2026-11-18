package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.PasswordToggle

// The shared ConfirmationDialog's input shows plain text, so destructive actions that need
// the account password use this instead.
@Composable
fun PasswordDialog(
    title: String,
    message: String,
    confirmLabel: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var masked by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = AppShapes.Card,
        containerColor = AppColors.Surface,
        title = { Text(title, style = AppTextStyles.Title) },
        text = {
            Column {
                Text(message, style = AppTextStyles.Body)
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Password",
                    error = error,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingContent = {
                        PasswordToggle(
                            isPasswordMasked = masked,
                            onToggle = { masked = !masked })
                    }
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = confirmLabel,
                onClick = onConfirm,
                loading = isLoading,
                containerColor = AppColors.Cancelled
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", style = AppTextStyles.Label.copy(color = AppColors.TextSecondary))
            }
        }
    )
}
