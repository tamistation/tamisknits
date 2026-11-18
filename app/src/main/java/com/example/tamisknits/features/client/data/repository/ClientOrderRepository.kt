package com.example.tamisknits.features.client.data.repository

import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStatuses
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientOrderRepository(
    private val firestore: FirebaseFirestore
) {

    // Sorted in memory so this query doesn't need a composite index.
    fun getClientOrders(clientId: String): Flow<ClientResult<List<ClientOrder>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Orders")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load your orders"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toClientOrder() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    fun getOrder(orderId: String): Flow<ClientResult<ClientOrder?>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Orders").document(orderId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { document, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load this order"))
                    return@addSnapshotListener
                }
                val order = document?.takeIf { it.exists() }?.toClientOrder()
                trySend(ClientResult.Success(order, document?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    // The order is written and the purchased cart lines are cleared in one batch,
    // so a failure never leaves a placed order with a still-full cart.
    fun placeOrder(
        clientId: String,
        items: List<OrderItem>,
        totalPrice: Double,
        shippingAddress: Map<String, String>,
        paymentMethod: String,
        orderNotes: String,
        cartIds: List<String>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val orderRef = firestore.collection("Orders").document()
        val data = hashMapOf(
            "clientId" to clientId,
            "deliveryId" to "",
            "items" to items.map { it.toFirestoreMap() },
            "totalPrice" to totalPrice,
            "status" to OrderStatuses.PENDING,
            "shippingAddress" to shippingAddress,
            "isCustomOrder" to false,
            "paymentMethod" to paymentMethod,
            "orderNotes" to orderNotes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val batch = firestore.batch()
        batch.set(orderRef, data)
        cartIds.forEach { cartId ->
            batch.delete(firestore.collection("Cart").document(cartId))
        }
        batch.commit()
            .addOnSuccessListener { onSuccess(orderRef.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to place order") }
    }

    fun cancelOrder(
        orderId: String,
        reason: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .update(
                mapOf(
                    "status" to OrderStatuses.CANCELLED,
                    "cancellationReason" to reason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to cancel order") }
    }
}
