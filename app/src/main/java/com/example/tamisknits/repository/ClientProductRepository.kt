package com.example.tamisknits.repository

import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.HomeCarousel
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

    // Ordered ids the admin picked for the home carousel. Any error (missing doc, rules) just
    // emits an empty list so the home screen falls back to its automatic picks.
    fun getHomeCarouselIds(): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection(HomeCarousel.COLLECTION)
            .document(HomeCarousel.DOCUMENT)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val ids = (document?.get(HomeCarousel.FIELD_PRODUCT_IDS) as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                trySend(ids)
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
