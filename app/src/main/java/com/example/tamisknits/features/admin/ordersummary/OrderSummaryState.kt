package com.example.tamisknits.features.admin.ordersummary

import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.OrderItem
import java.util.Collections.emptyList

data class OrderSummaryState(
    val order: Orders? = null,
    val items: List<OrderItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)