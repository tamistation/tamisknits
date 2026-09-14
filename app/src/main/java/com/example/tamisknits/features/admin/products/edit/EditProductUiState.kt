package com.example.tamisknits.features.admin.products.edit

import com.example.tamisknits.models.ProductVariant

data class EditProductUiState(
    val productId: String = "",

    val name: String = "",
    val description: String = "",
    val category: String = "Bags",
    val isCustomizable: Boolean = false,

    val imageUri: String? = null,

    val variants: List<ProductVariant> = emptyList(),

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val errorMessage: String? = null
)