package com.example.tamisknits.features.authentication.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles

// For failures the person can't fix by editing a field: wrong credentials, network or server errors.
@Composable
fun AuthErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = AppColors.Cancelled
            )
        },
        title = { Text("Something went wrong", style = AppTextStyles.Subtitle) },
        text = { Text(message, style = AppTextStyles.Body) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", style = AppTextStyles.Label.copy(color = AppColors.Terracotta))
            }
        },
        containerColor = AppColors.Surface,
        shape = AppShapes.Card
    )
}
