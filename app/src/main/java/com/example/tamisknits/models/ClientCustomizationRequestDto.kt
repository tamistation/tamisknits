package com.example.tamisknits.models


data class ClientCustomizationRequest(
    val request: CustomizationRequest = CustomizationRequest(),
    val referenceImageUrl: String = "",
    val createdAt: Long = 0L
) {
    val requestId: String get() = request.requestId
    val status: String get() = request.status
}

object CustomizationOptions {
    val colors = listOf(
        "Burgundy", "Cream", "Black", "Blush Pink", "Sage", "Mustard",
        "Navy", "Terracotta", "Lilac", "White"
    )
    val sizes = listOf("Mini", "Small", "Medium", "Large")

    fun statusLabel(status: String): String =
        status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
