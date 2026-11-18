package com.example.tamisknits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = AppShapes.Card,
    color: Color = AppColors.Surface,
    elevated: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val base = if (onClick != null) modifier.pressScale(interactionSource, 0.98f) else modifier
    val surface = base
        .then(
            if (elevated) Modifier.shadow(
                elevation = AppDimens.CardElevation,
                shape = shape,
                ambientColor = AppColors.Shadow,
                spotColor = AppColors.Shadow
            ) else Modifier
        )
        .clip(shape)
        .background(color)
        .border(1.dp, AppColors.OutlineSoft, shape)
        .then(
            if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.Terracotta),
                onClick = onClick
            ) else Modifier
        )

    Column(modifier = surface.padding(contentPadding), content = content)
}
