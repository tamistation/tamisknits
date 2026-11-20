package com.example.tamisknits.features.authentication.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tamisknits.features.authentication.components.BrandWordmark
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.StatusBarIcons
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    StatusBarIcons(dark = false)

    LaunchedEffect(Unit) {
        delay(2400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BrandBurgundy),
        contentAlignment = Alignment.Center
    ) {
        BrandWordmark()
    }
}
