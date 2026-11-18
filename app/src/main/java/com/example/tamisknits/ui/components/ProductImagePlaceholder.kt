package com.example.tamisknits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors

@Composable
fun ProductImagePlaceholder(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.Blush, AppColors.PlaceholderBottom)
                )
            )
            .drawBehind {
                val inset = 10.dp.toPx()
                val dash = 6.dp.toPx()
                drawRoundRect(
                    color = AppColors.Terracotta.copy(alpha = 0.18f),
                    topLeft = Offset(inset, inset),
                    size = Size(
                        size.width - inset * 2,
                        size.height - inset * 2
                    ),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash * 0.8f))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingBag,
            contentDescription = null,
            tint = AppColors.Terracotta.copy(alpha = 0.45f),
            modifier = Modifier.size(iconSize)
        )
    }
}
