package com.example.tamisknits.features.client.home

import com.example.tamisknits.models.ClientProduct

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val userName: String = "",
    val featured: List<ClientProduct> = emptyList(),
    val newArrivals: List<ClientProduct> = emptyList(),
    val bestSellers: List<ClientProduct> = emptyList(),
    val recentlyViewed: List<ClientProduct> = emptyList(),
    val categories: List<String> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val message: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && newArrivals.isEmpty()
}
