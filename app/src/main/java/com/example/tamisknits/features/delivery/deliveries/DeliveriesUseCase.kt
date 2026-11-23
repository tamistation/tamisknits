package com.example.tamisknits.features.delivery.deliveries

import com.example.tamisknits.features.delivery.data.repository.DeliveryOrderRepository
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.User
import com.example.tamisknits.repository.ClientAccountRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeliveriesUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository,
    private val deliveryOrderRepository: DeliveryOrderRepository
) {
    val currentUid: String
        get() = accountRepository.currentUid.orEmpty()

    // The driver's own User doc (for the greeting); the listener closes with the collector.
    fun getProfile(): Flow<ClientResult<User?>> = accountRepository.getProfile(currentUid)

    fun listenToMyOrders(
        onUpdate: (List<DeliveryOrder>) -> Unit,
        onFailure: (String) -> Unit
    ): ListenerRegistration =
        deliveryOrderRepository.listenToMyOrders(currentUid, onUpdate, onFailure)

    fun tabFor(status: String): DeliveryTab? = when (status) {
        OrderStatuses.CONFIRMED -> DeliveryTab.TO_PICK_UP
        OrderStatuses.SHIPPED, OrderStatuses.IN_TRANSIT -> DeliveryTab.ON_THE_WAY
        OrderStatuses.DELIVERED -> DeliveryTab.DONE
        // pending (not confirmed yet) / cancelled orders aren't actionable for the driver.
        else -> null
    }
}
