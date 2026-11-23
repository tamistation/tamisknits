package com.example.tamisknits.features.delivery.data.repository

import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.repository.millis
import com.example.tamisknits.repository.toClientOrder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

// Orders assigned to a driver live in the Orders collection with deliveryId == driver uid.
// Every write here must stay inside what firestore.rules allows the assigned driver to change:
// status (one step forward), updatedAt, deliveredAt, cashCollected, deliveryIssue.
class DeliveryOrderRepository(
    private val firestore: FirebaseFirestore
) {

    // Single equality filter, so no composite index is needed; status filtering and sorting
    // happen in the app. The caller must remove() the returned registration.
    fun listenToMyOrders(
        driverId: String,
        onUpdate: (List<DeliveryOrder>) -> Unit,
        onFailure: (String) -> Unit
    ): ListenerRegistration =
        firestore.collection("Orders")
            .whereEqualTo("deliveryId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error.message ?: "Couldn't load your deliveries")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toDeliveryOrder() }
                    ?.sortedByDescending { it.order.createdAt }
                    ?: emptyList()
                onUpdate(list)
            }

    // Live single order for the details screen. Allowed for the assigned driver by the Orders read rule.
    // Emits null if the order document is gone. If the admin reassigns the order, the rules deny
    // the read (PERMISSION_DENIED) and onUnassigned fires instead; Firestore then stops the listener.
    fun listenToOrder(
        orderId: String,
        onUpdate: (DeliveryOrder?) -> Unit,
        onUnassigned: () -> Unit,
        onFailure: (String) -> Unit
    ): ListenerRegistration =
        firestore.collection("Orders").document(orderId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) onUnassigned()
                    else onFailure(error.message ?: "Couldn't load this delivery")
                    return@addSnapshotListener
                }
                onUpdate(doc?.takeIf { it.exists() }?.toDeliveryOrder())
            }

    // Moves the order exactly one step along the driver flow. A plain update (not a transaction)
    // so it still queues while the driver is offline; the rules reject any illegal jump.
    fun advanceOrder(
        orderId: String,
        currentStatus: String,
        cashCollected: Boolean? = null,
        onSuccess: (newStatus: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val next = nextDriverStatus(currentStatus)
        if (next == null) {
            onFailure("This order can't be advanced from \"${OrderStatuses.label(currentStatus)}\"")
            return
        }
        val updates = hashMapOf<String, Any>(
            "status" to next,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (next == OrderStatuses.DELIVERED) updates["deliveredAt"] = FieldValue.serverTimestamp()
        if (cashCollected != null) updates["cashCollected"] = cashCollected

        firestore.collection("Orders").document(orderId)
            .update(updates)
            .addOnSuccessListener { onSuccess(next) }
            .addOnFailureListener { onFailure(it.message ?: "Couldn't update this order") }
    }

    fun reportIssue(
        orderId: String,
        issue: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .update(
                mapOf(
                    "deliveryIssue" to issue.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Couldn't report the issue") }
    }

    private fun DocumentSnapshot.toDeliveryOrder() = DeliveryOrder(
        order = toClientOrder(),
        deliveredAt = millis("deliveredAt"),
        cashCollected = getBoolean("cashCollected") ?: false,
        deliveryIssue = getString("deliveryIssue") ?: ""
    )

    companion object {
        // Driver flow, matching OrderStatusConfig.flow: confirmed -> shipped -> in_transit -> delivered.
        fun nextDriverStatus(current: String): String? = when (current) {
            OrderStatuses.CONFIRMED -> OrderStatuses.SHIPPED
            OrderStatuses.SHIPPED -> OrderStatuses.IN_TRANSIT
            OrderStatuses.IN_TRANSIT -> OrderStatuses.DELIVERED
            else -> null
        }

        // Button label for the action that performs nextDriverStatus(current).
        fun driverActionLabel(current: String): String? = when (current) {
            OrderStatuses.CONFIRMED -> "Picked up"
            OrderStatuses.SHIPPED -> "Start delivery"
            OrderStatuses.IN_TRANSIT -> "Mark delivered"
            else -> null
        }
    }
}
