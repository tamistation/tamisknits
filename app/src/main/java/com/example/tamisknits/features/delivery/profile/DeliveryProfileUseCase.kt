package com.example.tamisknits.features.delivery.profile

import com.example.tamisknits.features.delivery.data.repository.DeliveryOrderRepository
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.User
import com.example.tamisknits.repository.ClientAccountRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class DeliveryProfileUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository,
    private val deliveryOrderRepository: DeliveryOrderRepository
) {
    private val currentUid: String
        get() = accountRepository.currentUid.orEmpty()

    val currentEmail: String
        get() = accountRepository.currentEmail

    fun getProfile(): Flow<ClientResult<User?>> = accountRepository.getProfile(currentUid)

    // Same query as the Deliveries tab; Firestore shares identical active queries, so this
    // second listener doesn't cost extra reads.
    fun listenToMyOrders(
        onUpdate: (List<DeliveryOrder>) -> Unit,
        onFailure: (String) -> Unit
    ): ListenerRegistration =
        deliveryOrderRepository.listenToMyOrders(currentUid, onUpdate, onFailure)

    // Calendar week in the device's locale (Monday-first in Lebanon).
    fun deliveredThisWeek(orders: List<DeliveryOrder>, now: Long = System.currentTimeMillis()): Int {
        val weekStart = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis > now) add(Calendar.WEEK_OF_YEAR, -1)
        }.timeInMillis
        return orders.count { it.status == OrderStatuses.DELIVERED && it.deliveredAt >= weekStart }
    }

    fun signOut() = accountRepository.signOut()
}
