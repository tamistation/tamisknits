package com.example.tamisknits.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String = "See all",
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = AppTextStyles.Subtitle.copy(fontSize = AppTextStyles.Title.fontSize * 0.86f)
            )
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = AppTextStyles.Caption)
            }
        }
        if (onAction != null) {
            Row(
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    actionLabel,
                    style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(14.dp)
                )
            }
        }
    }
}
