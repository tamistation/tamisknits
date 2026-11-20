package com.example.tamisknits.features.client.checkout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.SecondaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OrderPlacedPage(
    orderId: String,
    onTrackOrder: () -> Unit,
    onContinueShopping: () -> Unit
) {
    val circleScale = remember { Animatable(0f) }
    val checkScale = remember { Animatable(0f) }
    val ring = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        circleScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 300f))
        launch { ring.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        checkScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
        delay(100)
        contentAlpha.animateTo(1f, tween(400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .drawBehind {
                        val progress = ring.value
                        if (progress > 0f) {
                            drawCircle(
                                color = AppColors.Terracotta.copy(alpha = 0.18f * (1f - progress)),
                                radius = size.minDimension / 2f * (0.55f + 0.45f * progress),
                                style = Stroke(width = 6.dp.toPx())
                            )
                            drawCircle(
                                color = AppColors.Blush,
                                radius = size.minDimension / 2f * 0.62f
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = circleScale.value
                            scaleY = circleScale.value
                        }
                        .clip(CircleShape)
                        .background(AppColors.Terracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppColors.OnTerracotta,
                        modifier = Modifier
                            .size(52.dp)
                            .graphicsLayer {
                                scaleX = checkScale.value
                                scaleY = checkScale.value
                            }
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha.value }
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Thank you!",
                    style = AppTextStyles.DisplayBrand,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Your order #${
                        orderId.take(8).uppercase()
                    } has been placed. We'll let you know as soon as it's confirmed.",
                    style = AppTextStyles.Body,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                PrimaryButton(
                    text = "Track my order",
                    icon = Icons.Rounded.LocalShipping,
                    onClick = onTrackOrder,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = "Continue shopping",
                    onClick = onContinueShopping,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
