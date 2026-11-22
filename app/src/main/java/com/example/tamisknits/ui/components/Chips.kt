package com.example.tamisknits.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles

@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingColor: Color? = null,
    enabled: Boolean = true
) {
    val container by animateColorAsState(
        if (selected) AppColors.Terracotta else AppColors.Surface,
        label = "chipContainer"
    )
    val content by animateColorAsState(
        when {
            !enabled -> AppColors.TextMuted.copy(alpha = 0.6f)
            selected -> AppColors.OnTerracotta
            else -> AppColors.TextDark
        },
        label = "chipContent"
    )
    val border by animateColorAsState(
        if (selected) AppColors.Terracotta else AppColors.Outline,
        label = "chipBorder"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .pressScale(interactionSource, 0.94f)
            .clip(AppShapes.Pill)
            .background(container)
            .border(1.dp, border, AppShapes.Pill)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingColor != null) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(leadingColor)
                    .border(1.dp, Color.White.copy(alpha = if (selected) 0.8f else 0f), CircleShape)
                    .border(0.5.dp, AppColors.Outline, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        } else if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = AppTextStyles.LabelSmall.copy(color = content),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ColorSwatch(
    colorName: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    available: Boolean = true,
    size: Dp = 40.dp,
    showLabel: Boolean = false
) {
    val color = AppColors.swatch(colorName)
    val ringWidth by animateDpAsState(if (selected) 2.dp else 0.dp, label = "swatchRing")
    val scale by animateFloatAsState(if (selected) 1f else 0.9f, label = "swatchScale")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(ringWidth, AppColors.Terracotta, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, AppColors.Outline, CircleShape)
                .clickable(onClick = onClick)
                .drawWithContent {
                    drawContent()
                    if (!available) {
                        drawLine(
                            color = AppColors.TextMuted,
                            start = Offset(0f, this.size.height),
                            end = Offset(this.size.width, 0f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.6f) AppColors.BrandBurgundy else Color.White,
                    modifier = Modifier.size(size * 0.4f)
                )
            }
        }
        if (showLabel) {
            Text(
                text = colorName,
                style = AppTextStyles.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(size + 24.dp)
                    .padding(top = 4.dp)
            )
        }
    }
}
