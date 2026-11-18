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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.TerracottaMuted,
                        AppColors.Terracotta,
                        AppColors.DarkTerracotta
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.95f, size.height * 0.05f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.height * 0.55f,
                    center = Offset(size.width * 0.82f, size.height * 1.05f)
                )
                val inset = 10.dp.toPx()
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.22f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                8.dp.toPx(),
                                6.dp.toPx()
                            )
                        )
                    )
                )
            }
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
                style = AppTextStyles.DisplayItalic.copy(
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
