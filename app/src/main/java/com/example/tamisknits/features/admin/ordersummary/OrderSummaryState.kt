package com.example.tamisknits.features.admin.ordersummary

import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.User
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
    val total: Double = 0.0,
    // driver assignment (Orders.deliveryId holds the assigned driver's uid)
    val drivers: List<User> = emptyList(),
    val isLoadingDrivers: Boolean = false,
    val showDriverPicker: Boolean = false,
    val isAssigningDriver: Boolean = false,
    val isClearingIssue: Boolean = false,
    val message: String? = null
) {
    val assignedDriverId: String
        get() = order?.deliveryId.orEmpty()

    val assignedDriver: User?
        get() = assignedDriverId.takeIf { it.isNotBlank() }
            ?.let { id -> drivers.find { it.uid == id } }

    // Inactive drivers stay in [drivers] so an existing assignment can still show its name.
    val assignableDrivers: List<User>
        get() = drivers.filter { it.status == "active" }
}
