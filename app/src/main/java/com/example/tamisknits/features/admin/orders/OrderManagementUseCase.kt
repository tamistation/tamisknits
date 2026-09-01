package com.example.tamisknits.features.admin.orders

import com.example.tamisknits.features.admin.ordersummary.OrderSummaryUseCase
import com.example.tamisknits.features.pricing.PricingUseCase
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderPricing
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.Orders
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//UI (Compose)  →  ViewModel  →  UseCase  →  Repository  →  Data source (Firestore/API/DB)
class OrderManagementUseCase @Inject constructor(

    private val repository: FirebaseRepository,//calling repo
    private val orderSummaryUseCase: OrderSummaryUseCase,
    private val pricingUseCase: PricingUseCase
) {
    fun getOrders(): Flow<List<Orders>> = repository.getOrders()
    //creates list of orders live from repo function get orders
    fun getOrderStatusConfig(): Flow<OrderStatusConfig> = repository.getOrderStatusConfig()

    //these update fuunctions are called by the viewmodel when it wants to update after ui
    suspend fun getOrderItemsWithProductInfo(
        items: List<Map<String, Any>>
    ) = orderSummaryUseCase.getOrderItemsWithProductInfo(items)

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
    fun calculatePricing(
        items: List<OrderItem>,
        discountPercentage: Double = 0.0
    ): OrderPricing {
        return pricingUseCase.calculatePricing(
            items,
            discountPercentage
        )
    }
}
//flow is used because its reactive and we need to collect the orders and orderstatus live mesh one time
