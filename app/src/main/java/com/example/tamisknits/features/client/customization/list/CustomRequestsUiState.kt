package com.example.tamisknits.features.client.customization.list

import com.example.tamisknits.models.ClientCustomizationRequest

data class CustomRequestsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val requests: List<ClientCustomizationRequest> = emptyList(),
    val productNames: Map<String, String> = emptyMap()
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && requests.isEmpty()
}
