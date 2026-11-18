package com.example.tamisknits.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tamisknits.models.OrderStep
import com.example.tamisknits.models.StepState
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppTextStyles

fun orderStatusIcon(status: String): ImageVector = when (status) {
    "pending" -> Icons.AutoMirrored.Rounded.ReceiptLong
    "confirmed" -> Icons.Rounded.ThumbUp
    "shipped" -> Icons.Rounded.Inventory2
    "in_transit" -> Icons.Rounded.LocalShipping
    "delivered" -> Icons.Rounded.Home
    else -> Icons.Rounded.RadioButtonUnchecked
}

fun orderStatusDescription(status: String): String = when (status) {
    "pending" -> "We've received your order and will confirm it shortly."
    "confirmed" -> "Your order is confirmed and being lovingly prepared."
    "shipped" -> "Packed with care and handed over for delivery."
    "in_transit" -> "Your parcel is on its way to you."
    "delivered" -> "Delivered. We hope you love it!"
    "cancelled" -> "This order was cancelled."
    else -> ""
}

@Composable
fun StatusTracker(steps: List<OrderStep>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            StatusStepRow(
                step = step,
                isLast = index == steps.lastIndex,
                nextState = steps.getOrNull(index + 1)?.state
            )
        }
    }
}

@Composable
private fun StatusStepRow(step: OrderStep, isLast: Boolean, nextState: StepState?) {
    val isDone = step.state == StepState.COMPLETED
    val isCurrent = step.state == StepState.CURRENT
    val circleColor by animateColorAsState(
        when (step.state) {
            StepState.COMPLETED, StepState.CURRENT -> AppColors.Terracotta
            StepState.UPCOMING -> AppColors.SurfaceMuted
        },
        animationSpec = tween(500),
        label = "stepColor"
    )
    val lineFill by animateFloatAsState(
        targetValue = if (isDone && nextState != StepState.UPCOMING) 1f else if (isDone) 0.5f else 0f,
        animationSpec = tween(700),
        label = "lineFill"
    )
    val pulse = rememberInfiniteTransition(label = "stepPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "pulseScale"
    )

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = (1.5f - pulseScale) * 0.8f
                            }
                            .clip(CircleShape)
                            .background(AppColors.Terracotta.copy(alpha = 0.25f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                        .border(
                            1.dp,
                            if (step.state == StepState.UPCOMING) AppColors.Outline else circleColor,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Rounded.Check else orderStatusIcon(step.status),
                        contentDescription = null,
                        tint = if (step.state == StepState.UPCOMING) AppColors.TextMuted else AppColors.OnTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .weight(1f)
                        .heightIn(min = 28.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.OutlineSoft)
                        .drawBehind {
                            drawRect(
                                color = AppColors.Terracotta,
                                size = size.copy(height = size.height * lineFill)
                            )
                        }
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 8.dp, bottom = if (isLast) 0.dp else 20.dp)
        ) {
            Text(
                text = step.label,
                style = if (step.state == StepState.UPCOMING) AppTextStyles.Label.copy(color = AppColors.TextMuted)
                else AppTextStyles.Label
            )
            if (step.state != StepState.UPCOMING) {
                Spacer(Modifier.height(2.dp))
                Text(orderStatusDescription(step.status), style = AppTextStyles.BodySmall)
            }
        }
    }
}
