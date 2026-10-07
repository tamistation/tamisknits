package com.example.tamisknits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors

@Composable
fun AuthenticationDivider(
    modifier: Modifier = Modifier,
    color: Color = AppColors.Terracotta
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp),

        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(3.dp)
                .background(color)
        )
    }
}


