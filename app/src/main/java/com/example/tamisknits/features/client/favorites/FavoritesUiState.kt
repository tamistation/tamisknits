package com.example.tamisknits.features.client.favorites

import com.example.tamisknits.models.ClientProduct

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val products: List<ClientProduct> = emptyList(),
    val movingToCartIds: Set<String> = emptySet(),
    val message: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && products.isEmpty()
}
