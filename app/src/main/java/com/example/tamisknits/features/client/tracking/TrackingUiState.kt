package com.example.tamisknits.features.client.tracking

import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.DeliveryEstimate
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStep
import com.example.tamisknits.models.SavedAddress

data class TrackingUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notFound: Boolean = false,
    val isOffline: Boolean = false,
    val order: ClientOrder? = null,
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0,
    val steps: List<OrderStep> = emptyList(),
    val progress: Float = 0f,
    val estimate: DeliveryEstimate? = null,
    val shippingAddress: SavedAddress = SavedAddress(),
    val isCancelled: Boolean = false,
    val isDelivered: Boolean = false
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}
