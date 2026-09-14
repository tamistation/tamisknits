package com.example.tamisknits.features.admin.products.add

import com.example.tamisknits.models.ProductVariant
import java.util.UUID

data class AddProductUiState(
    val name: String = "",
    val description: String = "",
    val category: String = "Bags",
    val isCustomizable: Boolean = false,

    val imageUri: String? = null,

    val variants: List<ProductVariant> = listOf(
        ProductVariant(
            variantId = UUID.randomUUID().toString()
        )
    ),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)