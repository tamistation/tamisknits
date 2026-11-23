package com.example.tamisknits.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// A one-way "slide to confirm" action for things that shouldn't happen on an accidental tap.
// Dragging the thumb past [threshold] of the track fires onConfirm; releasing earlier springs back.
// There is deliberately no reverse gesture.
@Composable
fun SwipeToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = AppColors.Terracotta,
    contentColor: Color = AppColors.OnTerracotta,
    threshold: Float = 0.85f
) {
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val active = enabled && !loading

    // Once the action finishes (or is re-enabled with a new label), park the thumb back at the start.
    LaunchedEffect(loading, text) {
        if (!loading) offsetX.animateTo(0f, spring())
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .clip(AppShapes.Button)
            .background(if (active || loading) containerColor else containerColor.copy(alpha = 0.35f))
            .semantics {
                // Accessibility services can't perform the drag, so expose it as a normal action.
                if (active) onClick(label = text) { currentOnConfirm(); true }
            }
    ) {
        val thumbSize = AppDimens.ButtonHeight - 8.dp
        val maxOffset = with(LocalDensity.current) { (maxWidth - thumbSize - 8.dp).toPx() }
        val progress = if (maxOffset > 0f) (offsetX.value / maxOffset).coerceIn(0f, 1f) else 0f

        Text(
            text = if (loading) "" else text,
            style = AppTextStyles.Button.copy(color = contentColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = thumbSize + 16.dp, end = 16.dp)
                .graphicsLayer { alpha = 1f - progress }
        )

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        }

        // Fill behind the thumb so the swipe reads as progress.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(4.dp)
                .size(width = with(LocalDensity.current) { offsetX.value.toDp() } + thumbSize, height = thumbSize)
                .clip(AppShapes.Button)
                .background(contentColor.copy(alpha = 0.18f))
        )

        Box(
            modifier = Modifier
                .padding(4.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(contentColor)
                .pointerInput(active, maxOffset) {
                    if (!active) return@pointerInput
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, maxOffset))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxOffset * threshold) {
                                    offsetX.animateTo(maxOffset, spring())
                                    currentOnConfirm()
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.6f))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring(dampingRatio = 0.6f)) }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = containerColor
            )
        }
    }
}
