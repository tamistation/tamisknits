package com.example.tamisknits.models

data class OrderPricingDto(
    val subtotal: Double,
    val deliveryFee: Double,
    val discountPercentage: Double,
    val discountAmount: Double,
    val total: Double
)