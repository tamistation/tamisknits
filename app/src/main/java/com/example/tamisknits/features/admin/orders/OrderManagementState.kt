package com.example.tamisknits.features.admin.orders

import com.example.tamisknits.models.Orders


data class OrderManagementUiState(
    val isLoading: Boolean = true,
    val allOrders: List<Orders> = emptyList(),
    val selectedTab: OrderTab = OrderTab.PENDING,
    val searchQuery: String = "",
    val selectedFilter: OrderFilter = OrderFilter.ALL,
    val cancelDialogOrderId: String? = null,
    val cancelReason: String = "",
    val draggingOrderId: String? = null
)

//val are readonly values
//the selected filter can only be of value of the olderfilter
//default one is set to All
//same thing for selected tabs