package com.example.tamisknits.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    drawBehind {
        val width = size.width
        val brush = Brush.linearGradient(
            colors = listOf(
                AppColors.ShimmerBase,
                AppColors.ShimmerHighlight,
                AppColors.ShimmerBase
            ),
            start = Offset(width * progress - width, 0f),
            end = Offset(width * progress, size.height)
        )
        drawRect(brush)
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.CardSmall
) {
    Box(modifier
        .clip(shape)
        .shimmer())
}
