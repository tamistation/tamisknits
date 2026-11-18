package com.example.tamisknits.features.client.data.repository

import com.example.tamisknits.models.ClientCustomizationRequest
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.CustomizationRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientCustomizationRepository(
    private val firestore: FirebaseFirestore
) {

    fun getRequests(clientId: String): Flow<ClientResult<List<ClientCustomizationRequest>>> =
        callbackFlow {
            trySend(ClientResult.Loading)
            val listener = firestore.collection("CustomizationRequests")
                .whereEqualTo("clientId", clientId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ClientResult.Error(error.message ?: "Couldn't load your requests"))
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents
                        ?.map { it.toClientCustomizationRequest() }
                        ?.sortedByDescending { it.createdAt }
                        ?: emptyList()
                    trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
                }
            awaitClose { listener.remove() }
        }

    // Same document shape as FirebaseRepository.addCustomizationRequest, plus referenceImageUrl.
    fun submitRequest(
        request: CustomizationRequest,
        referenceImageUrl: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "clientId" to request.clientId,
            "adminId" to request.adminId,
            "productId" to request.productId,
            "details" to request.details,
            "colorPreference" to request.colorPreference,
            "sizePreference" to request.sizePreference,
            "status" to request.status,
            "referenceImageUrl" to referenceImageUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("CustomizationRequests").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to send request") }
    }
}
