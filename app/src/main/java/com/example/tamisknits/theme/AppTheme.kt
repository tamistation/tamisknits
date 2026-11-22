package com.example.tamisknits.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.example.tamisknits.ui.components.LightSystemBars

@Composable
fun TamisKnitsTheme(content: @Composable () -> Unit) {
    LightSystemBars()
    MaterialTheme(colorScheme = lightScheme(), typography = appTypography(), content = content)
}

// Any Text without an explicit style (and Material components) picks up the brand fonts:
// Fraunces for display/headline/large titles, Nunito for everything else.
private fun appTypography(): Typography {
    val base = Typography()
    fun TextStyle.serif() = copy(fontFamily = Fraunces)
    fun TextStyle.sans() = copy(fontFamily = Nunito)
    return Typography(
        displayLarge = base.displayLarge.serif(),
        displayMedium = base.displayMedium.serif(),
        displaySmall = base.displaySmall.serif(),
        headlineLarge = base.headlineLarge.serif(),
        headlineMedium = base.headlineMedium.serif(),
        headlineSmall = base.headlineSmall.serif(),
        titleLarge = base.titleLarge.serif(),
        titleMedium = base.titleMedium.sans(),
        titleSmall = base.titleSmall.sans(),
        bodyLarge = base.bodyLarge.sans(),
        bodyMedium = base.bodyMedium.sans(),
        bodySmall = base.bodySmall.sans(),
        labelLarge = base.labelLarge.sans(),
        labelMedium = base.labelMedium.sans(),
        labelSmall = base.labelSmall.sans()
    )
}

private fun lightScheme(): ColorScheme = lightColorScheme(
    primary = AppColors.Terracotta,
    onPrimary = AppColors.OnTerracotta,
    secondary = AppColors.MarigoldGold,
    background = AppColors.Cream,
    onBackground = AppColors.TextDark,
    surface = AppColors.Surface,
    onSurface = AppColors.TextDark,
    surfaceVariant = AppColors.SurfaceMuted,
    onSurfaceVariant = AppColors.TextSecondary,
    surfaceContainer = AppColors.Surface,
    surfaceContainerHigh = AppColors.Surface,
    surfaceContainerHighest = AppColors.SurfaceMuted,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineSoft,
    error = AppColors.Cancelled
)
