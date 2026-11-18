package com.example.tamisknits.features.client.orders.details

import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.DeliveryEstimate
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderPricingDto
import com.example.tamisknits.models.OrderStep
import com.example.tamisknits.models.SavedAddress

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notFound: Boolean = false,
    val isOffline: Boolean = false,
    val order: ClientOrder? = null,
    val items: List<OrderItem> = emptyList(),
    val pricing: OrderPricingDto? = null,
    val shippingAddress: SavedAddress = SavedAddress(),
    val steps: List<OrderStep> = emptyList(),
    val estimate: DeliveryEstimate? = null,
    val canCancel: Boolean = false,
    val showCancelDialog: Boolean = false,
    val cancelReason: String = "",
    val isCancelling: Boolean = false,
    val message: String? = null
)
