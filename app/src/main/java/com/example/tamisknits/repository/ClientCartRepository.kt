package com.example.tamisknits.repository

import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.ClientResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientCartRepository(
    private val firestore: FirebaseFirestore
) {

    fun getCart(clientId: String): Flow<ClientResult<List<CartLine>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Cart")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load your cart"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toCartLine() }
                    ?.sortedBy { it.addedAt }
                    ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    // Adding the same variant twice bumps the existing line instead of creating a duplicate.
    fun addToCart(
        clientId: String,
        productId: String,
        variantId: String,
        quantity: Int,
        unitPrice: Double,
        maxStock: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val cart = firestore.collection("Cart")
        cart.whereEqualTo("clientId", clientId)
            .whereEqualTo("productId", productId)
            .whereEqualTo("variantId", variantId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val existing = snapshot.documents.firstOrNull()
                if (existing != null) {
                    val current = (existing.get("quantity") as? Number)?.toInt() ?: 0
                    val newQuantity = (current + quantity).coerceAtMost(maxStock.coerceAtLeast(1))
                    existing.reference
                        .update(
                            mapOf(
                                "quantity" to newQuantity,
                                "totalPrice" to unitPrice * newQuantity
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it.message ?: "Failed to update cart") }
                } else {
                    val data = hashMapOf(
                        "clientId" to clientId,
                        "productId" to productId,
                        "variantId" to variantId,
                        "quantity" to quantity,
                        "totalPrice" to unitPrice * quantity,
                        "deliveryId" to "",
                        "addedAt" to FieldValue.serverTimestamp()
                    )
                    cart.add(data)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it.message ?: "Failed to add to cart") }
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to cart") }
    }

    fun changeVariant(
        cartId: String,
        variantId: String,
        unitPrice: Double,
        quantity: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Cart").document(cartId)
            .update(
                mapOf(
                    "variantId" to variantId,
                    "quantity" to quantity,
                    "totalPrice" to unitPrice * quantity
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update cart") }
    }
}
