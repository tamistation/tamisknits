package com.example.tamisknits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors

@Composable
fun DragToAdvanceRow(
    itemKey: Any,
    onAdvance: () -> Unit,
    onGoBack: () -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    label: String = "Drag → next status",
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.DragBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .pointerInput(itemKey) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { _, dragAmount ->
                        dragOffsetX += dragAmount.x
                        if (dragOffsetX > 40f) {
                            onAdvance()
                            dragOffsetX = 0f
                        }
                        if (dragOffsetX < -40f) {
                            onGoBack()
                            dragOffsetX = 0f
                        }
                    },
                    onDragEnd = {
                        dragOffsetX = 0f
                        onDragEnd()
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                        onDragEnd()
                    },
                )
            },
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = "Drag to advance status",
            tint = AppColors.TextMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = AppColors.TextMuted)
    }
}