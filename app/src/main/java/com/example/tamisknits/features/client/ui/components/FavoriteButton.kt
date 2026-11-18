package com.example.tamisknits.features.client.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.pressScale
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 38.dp,
    background: Color = Color.White.copy(alpha = 0.94f)
) {
    val haptics = LocalHapticFeedback.current
    val iconScale = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    var hasSettled by remember { mutableStateOf(false) }

    // Skip the first composition so hearts don't all pop when a list first appears.
    LaunchedEffect(isFavorite) {
        if (!hasSettled) {
            hasSettled = true
            return@LaunchedEffect
        }
        if (isFavorite) {
            launch {
                burst.snapTo(0f)
                burst.animateTo(1f, tween(durationMillis = 480))
            }
        }
        iconScale.snapTo(0.6f)
        iconScale.animateTo(1f, spring(dampingRatio = 0.32f, stiffness = 380f))
    }

    val tint by animateColorAsState(
        targetValue = if (isFavorite) AppColors.Terracotta else AppColors.TextSecondary,
        label = "heartTint"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(buttonSize)
            .drawBehind {
                val progress = burst.value
                if (progress < 1f) {
                    val radius = this.size.minDimension / 2f
                    drawCircle(
                        color = AppColors.Terracotta.copy(alpha = 0.4f * (1f - progress)),
                        radius = radius * (0.7f + progress * 0.6f),
                        style = Stroke(width = 2.dp.toPx() * (1f - progress) + 0.5f)
                    )
                }
            }
            .pressScale(interactionSource, 0.88f)
            .shadow(
                3.dp,
                CircleShape,
                ambientColor = AppColors.Shadow,
                spotColor = AppColors.Shadow
            )
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.Terracotta),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = tint,
            modifier = Modifier
                .size(buttonSize * 0.52f)
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                }
        )
    }
}
