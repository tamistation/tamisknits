package com.example.tamisknits.repository

import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.SavedAddress
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientAddressRepository(
    private val firestore: FirebaseFirestore
) {

    private fun addressesOf(clientId: String): CollectionReference =
        firestore.collection("User").document(clientId).collection("addresses")

    fun getAddresses(clientId: String): Flow<ClientResult<List<SavedAddress>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = addressesOf(clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load your addresses"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toSavedAddress() }
                    ?.sortedWith(compareByDescending<SavedAddress> { it.isDefault }.thenBy { it.label })
                    ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    fun saveAddress(
        clientId: String,
        address: SavedAddress,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val collection = addressesOf(clientId)
        val ref = if (address.addressId.isBlank()) collection.document()
        else collection.document(address.addressId)
        val data = address.toFirestoreMap()

        if (!address.isDefault) {
            ref.set(data)
                .addOnSuccessListener { onSuccess(ref.id) }
                .addOnFailureListener { onFailure(it.message ?: "Failed to save address") }
            return
        }

        collection.whereEqualTo("isDefault", true).get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents
                    .filter { it.id != ref.id }
                    .forEach { batch.update(it.reference, "isDefault", false) }
                batch.set(ref, data)
                batch.commit()
                    .addOnSuccessListener { onSuccess(ref.id) }
                    .addOnFailureListener { onFailure(it.message ?: "Failed to save address") }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to save address") }
    }

    fun setDefault(
        clientId: String,
        addressId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val collection = addressesOf(clientId)
        collection.get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", doc.id == addressId)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message ?: "Failed to update address") }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update address") }
    }

    fun deleteAddress(
        clientId: String,
        addressId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        addressesOf(clientId).document(addressId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete address") }
    }
}
