package com.example.tamisknits.features.client.orders.list

import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientOrderTab
import com.example.tamisknits.models.OrderItem

data class OrderListItem(
    val order: ClientOrder,
    val items: List<OrderItem>,
    val total: Double,
    val progress: Float = 0f
)

data class OrdersUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val selectedTab: ClientOrderTab = ClientOrderTab.ACTIVE,
    val orders: List<OrderListItem> = emptyList(),
    val counts: Map<ClientOrderTab, Int> = emptyMap()
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && orders.isEmpty()
}
