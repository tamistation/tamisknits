package com.example.tamisknits.models


data class Cart(
    val cartId: String = "",
    val clientId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val deliveryId: String = ""
)