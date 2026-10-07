package com.example.tamisknits.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors


@Composable
fun Loader(
    modifier: Modifier = Modifier,
    color: Color = AppColors.Terracotta
) {
    CircularProgressIndicator(
        modifier = modifier.size(44.dp),
        color = color
    )
}