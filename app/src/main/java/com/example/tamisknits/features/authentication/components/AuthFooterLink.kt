package com.example.tamisknits.features.authentication.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppTextStyles

@Composable
fun AuthFooterLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(prompt, style = AppTextStyles.Body)
        TextButton(onClick = onClick) {
            Text(
                action,
                style = AppTextStyles.Label.copy(color = AppColors.Terracotta),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
