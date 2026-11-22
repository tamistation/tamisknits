package com.example.tamisknits.repository

import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.ProductReview
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientReviewRepository(
    private val firestore: FirebaseFirestore
) {

    fun getReviews(productId: String): Flow<ClientResult<List<ProductReview>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("Reviews")
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load reviews"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toProductReview() }
                    ?.sortedByDescending { maxOf(it.createdAt, it.updatedAt) }
                    ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    // One review per client per product: the document id is derived from both,
    // so saving again edits the existing review.
    fun saveReview(
        review: ProductReview,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>(
            "productId" to review.productId,
            "clientId" to review.clientId,
            "clientName" to review.clientName,
            "rating" to review.rating.coerceIn(1, 5),
            "comment" to review.comment,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (review.createdAt == 0L) data["createdAt"] = FieldValue.serverTimestamp()

        firestore.collection("Reviews").document(reviewId(review.productId, review.clientId))
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to save review") }
    }

    fun deleteReview(
        productId: String,
        clientId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Reviews").document(reviewId(productId, clientId))
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete review") }
    }

    private fun reviewId(productId: String, clientId: String) = "${productId}_$clientId"
}
