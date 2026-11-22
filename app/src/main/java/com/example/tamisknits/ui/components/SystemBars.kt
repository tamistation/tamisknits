package com.example.tamisknits.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Forces the status and navigation bar icon colour while the calling screen is shown and
// restores it afterwards. Pass dark = true on light backgrounds and dark = false on burgundy ones;
// navigationDark covers screens whose bottom edge differs from the top.
@Composable
fun StatusBarIcons(dark: Boolean, navigationDark: Boolean = dark) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view, dark, navigationDark) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = dark
        controller?.isAppearanceLightNavigationBars = navigationDark
        onDispose {
            if (controller != null && previousStatus != null && previousNav != null) {
                controller.isAppearanceLightStatusBars = previousStatus
                controller.isAppearanceLightNavigationBars = previousNav
            }
        }
    }
}

// App-wide default: dark system bar icons on the light theme.
// Screens with their own background (splash, sign-in) override it with StatusBarIcons.
@Composable
fun LightSystemBars() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
        onDispose { }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
