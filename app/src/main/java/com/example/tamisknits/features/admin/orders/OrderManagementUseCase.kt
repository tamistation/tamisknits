package com.example.tamisknits.features.admin.orders

import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.Orders
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OrderManagementUseCase @Inject constructor(

    private val repository: FirebaseRepository //calling repo
) {
    fun getOrders(): Flow<List<Orders>> = repository.getOrders()

    //creates list of orders live from repo function get orders
    fun getOrderStatusConfig(): Flow<OrderStatusConfig> = repository.getOrderStatusConfig()

    //these update fuunctions are called by the viewmodel when it wants to update after ui
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.updateOrderStatus(
            orderId, newStatus, onSuccess,
            onFailure
        )
        // and sent to repo to update
    }

    fun updateOrder(
        orderId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.updateOrder(orderId, updates, onSuccess, onFailure)
    }
}
//flow is used because its reactive and we need to collect the orders and orderstatus live mesh one time
