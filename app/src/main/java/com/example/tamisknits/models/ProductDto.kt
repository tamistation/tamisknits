package com.example.tamisknits.models

import java.util.Collections.emptyList

data class ProductVariant(
    val variantId: String = "",
    val size: String = "",
    val color: String = "",
    val stock: Int = 0,
    val imageUrl: String = "",
    val price: Double = 0.0
)

data class Products(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val isCustomizable: Boolean = false,
    val variants: List<ProductVariant> = emptyList()
)

//object variant for color,size,
//custom function for order/products rendering product info
//i need to add subtotal and total (before and after delivery)+discount code thing
