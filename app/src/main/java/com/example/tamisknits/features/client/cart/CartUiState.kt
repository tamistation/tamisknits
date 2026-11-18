package com.example.tamisknits.features.client.cart

import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.OrderPricingDto

data class CartUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val lines: List<CartLine> = emptyList(),
    val pricing: OrderPricingDto? = null,
    val updatingCartIds: Set<String> = emptySet(),
    val lastRemoved: CartLine? = null,
    val message: String? = null
) {
    val itemCount: Int get() = lines.sumOf { it.item.quantity }
    val hasIssues: Boolean get() = lines.any { !it.isAvailable || it.exceedsStock }
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && lines.isEmpty()
    val canCheckout: Boolean get() = lines.isNotEmpty() && !hasIssues
}
