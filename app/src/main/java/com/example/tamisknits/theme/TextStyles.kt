package com.example.tamisknits.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppType
import com.example.tamisknits.theme.Fraunces
import com.example.tamisknits.theme.Nunito

object AppTextStyles {
    val Display = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = AppColors.TextDark
    )
    val DisplayItalic = Display.copy(fontStyle = FontStyle.Italic)
    val Wordmark = AppType.WordmarkItalic.copy(color = AppColors.Terracotta)
    val Title = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = AppColors.TextDark
    )
    val Subtitle = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = AppColors.TextDark
    )
    val Body = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AppColors.TextSecondary
    )
    val BodySmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = AppColors.TextSecondary
    )
    val Label = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = AppColors.TextDark
    )
    val LabelSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = AppColors.TextSecondary
    )
    val Caption = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = AppColors.TextMuted
    )
    val Overline = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
        color = AppColors.TextMuted
    )
    val Price = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = AppColors.Terracotta
    )
    val PriceLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        color = AppColors.Terracotta
    )
    val Button = AppType.Button
}
