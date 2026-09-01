package com.example.tamisknits.models

data class OrderPricing(
    val subtotal: Double,
    val deliveryFee: Double,
    val discountPercentage: Double,
    val discountAmount: Double,
    val total: Double
)