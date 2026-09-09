package com.example.tamisknits.features.admin.products

import com.example.tamisknits.models.Products

data class ProductManagementUiState(
    val isLoading: Boolean = true,
    val allProducts: List<Products> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val deleteDialogProductId: String? = null
)