package com.example.tamisknits.models

data class Product(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val stock: Int = 0,
    val isCustomizable: Boolean = false
)