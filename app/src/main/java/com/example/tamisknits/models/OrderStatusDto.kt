package com.example.tamisknits.models


data class OrderStatusConfig(
    val pending: List<String> = listOf("pending"),

    val active: List<String> = listOf("confirmed", "shipped", "in_transit"),

    val previous: List<String> = listOf("delivered", "cancelled"),

    val flow: List<String> = listOf("pending", "confirmed", "shipped", "in_transit", "delivered")

)