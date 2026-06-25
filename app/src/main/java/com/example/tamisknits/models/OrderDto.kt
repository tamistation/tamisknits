package com.example.tamisknits.models


data class Order(
    val orderId: String = "",
    val clientId: String = "",
    val deliveryId: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "pending",
    val shippingAddress: Map<String, String> = emptyMap(),
    val isCustomOrder: Boolean
)
