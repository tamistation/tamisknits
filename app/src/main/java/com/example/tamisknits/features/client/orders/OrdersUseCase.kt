package com.example.tamisknits.features.client.orders

import com.example.tamisknits.features.admin.ordersummary.OrderSummaryUseCase
import com.example.tamisknits.features.client.data.repository.ClientOrderRepository
import com.example.tamisknits.features.pricing.PricingUseCase
import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderPricingDto
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class OrdersUseCase @Inject constructor(
    private val orderRepository: ClientOrderRepository,
    private val repository: FirebaseRepository,
    private val orderSummaryUseCase: OrderSummaryUseCase,
    private val pricingUseCase: PricingUseCase
) {
    fun getOrders(clientId: String): Flow<ClientResult<List<ClientOrder>>> =
        orderRepository.getClientOrders(clientId)

    fun getOrder(orderId: String): Flow<ClientResult<ClientOrder?>> =
        orderRepository.getOrder(orderId)

    fun getOrderStatusConfig(): Flow<OrderStatusConfig> = repository.getOrderStatusConfig()

    // Orders placed from the client already carry name/price/image per item. Older or admin-created
    // orders only have ids, so those are filled in the same way the admin order summary does it.
    suspend fun resolveItems(order: ClientOrder): List<OrderItem> {
        val complete = order.items.isNotEmpty() &&
                order.items.all { it.name.isNotBlank() && it.price > 0.0 }
        return if (complete) order.items
        else orderSummaryUseCase.getOrderItemsWithProductInfo(order.order.items)
    }

    fun calculatePricing(items: List<OrderItem>): OrderPricingDto =
        pricingUseCase.calculatePricing(items)

    fun cancelOrder(
        orderId: String,
        reason: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = orderRepository.cancelOrder(orderId, reason, onSuccess, onFailure)
}
