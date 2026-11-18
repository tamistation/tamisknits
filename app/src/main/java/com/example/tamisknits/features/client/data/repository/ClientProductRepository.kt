package com.example.tamisknits.features.client.data.repository

import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientProductRepository(
    private val firestore: FirebaseFirestore
) {

    // MetadataChanges.INCLUDE re-emits when the data switches between cache and server,
    // which is what drives the offline banner.
    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Products")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load products"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { it.toClientProduct() } ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    fun getProduct(productId: String): Flow<ClientResult<ClientProduct?>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Products").document(productId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { document, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load this product"))
                    return@addSnapshotListener
                }
                val product = document?.takeIf { it.exists() }?.toClientProduct()
                trySend(ClientResult.Success(product, document?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }
}
