package com.example.tamisknits.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTextStyles {
    val Display
        get() = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = AppColors.TextDark
    )
    val DisplayBrand
        get() = AppType.Brand.copy(
            fontSize = 32.sp,
            lineHeight = 38.sp,
            color = AppColors.TextDark
        )
    val Wordmark get() = AppType.Brand.copy(color = AppColors.Terracotta)
    val Title
        get() = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = AppColors.TextDark
    )
    val Subtitle
        get() = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = AppColors.TextDark
    )
    val Body
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AppColors.TextSecondary
    )
    val BodySmall
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = AppColors.TextSecondary
    )
    val Label
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = AppColors.TextDark
    )
    val LabelSmall
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = AppColors.TextSecondary
    )
    val Caption
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = AppColors.TextMuted
    )
    val Overline
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
        color = AppColors.TextMuted
    )
    val Price
        get() = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = AppColors.Terracotta
    )
    val PriceLarge
        get() = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        color = AppColors.Terracotta
    )
    val Button get() = AppType.Button
}
