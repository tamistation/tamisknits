package com.example.tamisknits.features.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.pressScale

@Composable
fun PromoBanner(
    overline: String,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource, 0.98f)
            .clip(AppShapes.Card)
            .background(AppColors.BrandBurgundy)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onAction)
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.MarigoldGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    overline.uppercase(),
                    style = AppTextStyles.Overline.copy(color = AppColors.BlushDeep)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = AppTextStyles.DisplayBrand.copy(
                    color = Color.White,
                    fontSize = AppTextStyles.Title.fontSize
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                style = AppTextStyles.BodySmall.copy(color = Color.White.copy(alpha = 0.82f))
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    actionLabel,
                    style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
