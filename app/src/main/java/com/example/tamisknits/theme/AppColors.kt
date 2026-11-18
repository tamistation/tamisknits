package com.example.tamisknits.theme

import androidx.compose.ui.graphics.Color


object AppColors {
    val Cream = Color(0xFFFDFBF9)
    val Terracotta = Color(0xFF4D0E13)
    val DarkTerracotta = Color(0xFF26070A)
    val TextDark = Color(0xFF1A0A10)
    val TextMuted = Color(0xFF9C8490)
    val Pending = Color(0xFFB5892A)
    val Active = Color(0xFF3D6B8C)
    val Delivered = Color(0xFF3A7D5C)
    val Cancelled = Color(0xFF8C1C1C)
    val DividerLight = Color(0xFFE5DDD5)
    val Outline = Color(0xFFE0D8D0)
    val DividerLightTwo = Color(0xFFF0EBE5)
    val DragBackground = Color(0xFFF5F0EB)
    val CancelButtonBackground = Color(0xFFFFF0EE)
    val Surface = Color.White
    val UnknownStatus = Color.LightGray

    val MarigoldGold = Color(0xFFD9A441)

    val Blush = Color(0xFFF6E8E6)

    val SageThread = Color(0xFF6E8266)


    val SuccessGreen = Color(0xFF4CAF50)
    val SuccessGreenSoft = Color(0xFFE8F5E9)


    val ErrorRed = Color(0xFFF44336)
    val ErrorRedSoft = Color(0xFFFFEBEE)


    val InfoBlue = Color(0xFF2196F3)

    val TerracottaMuted = Color(0xFF7A2E35)
    val OnTerracotta = Color.White
    val BlushDeep = Color(0xFFEFD9D6)
    val SurfaceMuted = Color(0xFFFAF5F3)
    val OutlineSoft = Color(0xFFF1E7E4)
    val TextSecondary = Color(0xFF6B5560)
    val PendingSoft = Color(0xFFF8EFDC)
    val DeliveredSoft = Color(0xFFE6F2EC)
    val ShimmerBase = Color(0xFFF4ECEA)
    val ShimmerHighlight = Color(0xFFFCF8F7)
    val PlaceholderBottom = Color(0xFFEAD2CF)
    val Scrim = Color(0x66000000)
    val Shadow = Color(0x334D0E13)

    fun orderStatusColor(status: String): Color = when (status) {
        "pending" -> Pending
        "confirmed", "shipped", "in_transit" -> Active
        "delivered" -> Delivered
        "cancelled" -> Cancelled
        else -> TextMuted
    }

    fun requestStatusColor(status: String): Color = when (status) {
        "pending", "open" -> Pending
        "approved", "accepted", "active", "in_progress" -> Active
        "completed", "done" -> Delivered
        "rejected", "declined", "cancelled" -> Cancelled
        else -> TextMuted
    }

    // Maps common colour names admins type into variants to swatches; unknown names get a neutral swatch.
    fun swatch(name: String): Color = when (name.trim().lowercase()) {
        "burgundy", "wine", "maroon" -> Color(0xFF6D1A24)
        "red" -> Color(0xFFB3262E)
        "pink", "blush", "blush pink", "rose" -> Color(0xFFF2C4C8)
        "cream", "ivory", "beige", "off white" -> Color(0xFFF3E9D8)
        "white" -> Color(0xFFFFFFFF)
        "black" -> Color(0xFF1E1A1B)
        "brown", "chocolate", "camel" -> Color(0xFF8A5A3C)
        "terracotta", "rust" -> Color(0xFFC0623F)
        "orange" -> Color(0xFFE38B3C)
        "mustard", "yellow" -> Color(0xFFD9A441)
        "green", "olive" -> Color(0xFF5E7A45)
        "sage" -> Color(0xFF9CAF88)
        "blue" -> Color(0xFF3D6B8C)
        "navy" -> Color(0xFF22324F)
        "purple", "lilac", "lavender" -> Color(0xFFB59CCB)
        "grey", "gray" -> Color(0xFF9E9A9B)
        else -> Color(0xFFD8CCC9)
    }
}