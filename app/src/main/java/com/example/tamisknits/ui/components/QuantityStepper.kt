package com.example.tamisknits.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrease: Boolean = quantity > 1,
    canIncrease: Boolean = true,
    compact: Boolean = false
) {
    val buttonSize = if (compact) 30.dp else 38.dp
    Row(
        modifier = modifier
            .clip(AppShapes.Pill)
            .background(AppColors.SurfaceMuted),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepButton(Icons.Rounded.Remove, "Decrease quantity", canDecrease, buttonSize, onDecrease)
        AnimatedContent(
            targetState = quantity,
            transitionSpec = {
                val up = targetState > initialState
                ContentTransform(
                    targetContentEnter = slideInVertically { if (up) it else -it } + fadeIn(),
                    initialContentExit = slideOutVertically { if (up) -it else it } + fadeOut(),
                    sizeTransform = SizeTransform(clip = false)
                )
            },
            label = "quantity"
        ) { value ->
            Text(
                text = value.toString(),
                style = AppTextStyles.Label,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = if (compact) 24.dp else 32.dp)
            )
        }
        StepButton(Icons.Rounded.Add, "Increase quantity", canIncrease, buttonSize, onIncrease)
    }
}

@Composable
private fun StepButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) AppColors.Terracotta else AppColors.TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
