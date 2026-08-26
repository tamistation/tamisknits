package com.example.tamisknits.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PasswordToggle(
    isPasswordMasked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isPasswordMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = if (isPasswordMasked) "Show password" else "Hide password",
        )
    }
}