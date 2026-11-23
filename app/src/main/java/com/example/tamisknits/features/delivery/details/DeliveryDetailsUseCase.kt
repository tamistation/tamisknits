package com.example.tamisknits.features.delivery.details

import com.example.tamisknits.features.delivery.data.repository.DeliveryOrderRepository
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.repository.OrderRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeliveryDetailsUseCase @Inject constructor(
    private val deliveryOrderRepository: DeliveryOrderRepository,
    private val orderRepository: OrderRepository
) {
    fun listenToOrder(
        orderId: String,
        onUpdate: (DeliveryOrder?) -> Unit,
        onFailure: (String) -> Unit
    ): ListenerRegistration = deliveryOrderRepository.listenToOrder(orderId, onUpdate, onFailure)

    // Same config/orderStatuses doc the client tracker uses, so both show the same steps.
    fun getOrderStatusConfig(): Flow<OrderStatusConfig> = orderRepository.getOrderStatusConfig()

    fun nextStatus(current: String): String? = DeliveryOrderRepository.nextDriverStatus(current)

    fun actionLabel(current: String): String? = DeliveryOrderRepository.driverActionLabel(current)

    fun advanceOrder(
        orderId: String,
        currentStatus: String,
        cashCollected: Boolean?,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = deliveryOrderRepository.advanceOrder(orderId, currentStatus, cashCollected, onSuccess, onFailure)

    fun reportIssue(
        orderId: String,
        issue: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = deliveryOrderRepository.reportIssue(orderId, issue, onSuccess, onFailure)
}
