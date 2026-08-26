package com.example.tamisknits.models

data class OrderItem(
    val productId: String = "",
    val quantity: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
    val size: String = "",
    val color: String = "",
    val price: Double = 0.0
)