package com.example.tamisknits.features.admin.ordersummary

import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.Orders
import java.util.Collections.emptyList

data class OrderSummaryState(
    val order: Orders? = null,
    val items: List<OrderItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val discountAmount: Double = 0.0,
    val total: Double = 0.0
)