package com.example.tamisknits.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors

@Composable
fun RatingStars(rating: Double, modifier: Modifier = Modifier, starSize: Dp = 14.dp) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        (1..5).forEach { index ->
            val icon = when {
                rating >= index -> Icons.Rounded.Star
                rating >= index - 0.5 -> Icons.AutoMirrored.Rounded.StarHalf
                else -> Icons.Rounded.StarOutline
            }
            Icon(
                icon,
                contentDescription = null,
                tint = AppColors.MarigoldGold,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun RatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..5).forEach { index ->
            val selected = index <= rating
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.4f),
                label = "starScale"
            )
            Icon(
                imageVector = if (selected) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "$index star${if (index > 1) "s" else ""}",
                tint = if (selected) AppColors.MarigoldGold else AppColors.Outline,
                modifier = Modifier
                    .size(starSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .clickable { onRatingChange(index) }
            )
        }
    }
}
