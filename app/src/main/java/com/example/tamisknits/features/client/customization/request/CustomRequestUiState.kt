package com.example.tamisknits.features.client.customization.request

import com.example.tamisknits.models.CustomizationOptions

enum class CustomRequestField { COLOR, SIZE, DETAILS, LINK }

data class CustomRequestUiState(
    val productId: String = "",
    val productName: String = "",
    val colorOptions: List<String> = CustomizationOptions.colors,
    val sizeOptions: List<String> = CustomizationOptions.sizes,
    val color: String = "",
    val size: String = "",
    val details: String = "",
    val referenceLink: String = "",
    val fieldErrors: Map<CustomRequestField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submittedRequestId: String? = null,
    val message: String? = null
) {
    val detailsRemaining: Int
        get() = MAX_DETAILS_LENGTH - details.length

    companion object {
        const val MIN_DETAILS_LENGTH = 20
        const val MAX_DETAILS_LENGTH = 1000
    }
}
