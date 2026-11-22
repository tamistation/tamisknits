package com.example.tamisknits.features.admin.homecarousel

import com.example.tamisknits.models.HomeCarousel
import com.example.tamisknits.models.Products

data class HomeCarouselUiState(
    val isLoading: Boolean = true,
    val allProducts: List<Products> = emptyList(),
    // The admin's working list, in carousel order. Only written to Firestore on Save.
    val selectedIds: List<String> = emptyList(),
    val savedIds: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null
) {
    private val byId: Map<String, Products> get() = allProducts.associateBy { it.productId }

    // Picked products that still exist, in order (a deleted product silently drops out).
    val selectedProducts: List<Products> get() = selectedIds.mapNotNull { byId[it] }

    val availableProducts: List<Products>
        get() = allProducts.filter { it.productId !in selectedIds }.sortedBy { it.name.lowercase() }

    val hasChanges: Boolean get() = selectedIds != savedIds
    val isFull: Boolean get() = selectedProducts.size >= HomeCarousel.MAX_PRODUCTS
}
