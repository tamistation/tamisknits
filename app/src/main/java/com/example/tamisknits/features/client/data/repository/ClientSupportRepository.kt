package com.example.tamisknits.features.client.data.repository

import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.ClientTicket
import com.example.tamisknits.models.SupportTickets
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Creating tickets and sending/reading chat messages go through the existing FirebaseRepository;
// this only adds the client-scoped live reads that report errors and creation time.
class ClientSupportRepository(
    private val firestore: FirebaseFirestore
) {

    fun getTickets(clientId: String): Flow<ClientResult<List<ClientTicket>>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("SupportTickets")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load your tickets"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.map { it.toClientTicket() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(ClientResult.Success(list, snapshot?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    fun getTicket(ticketId: String): Flow<ClientResult<SupportTickets?>> = callbackFlow {
        trySend(ClientResult.Loading)
        val listener = firestore.collection("SupportTickets").document(ticketId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load this ticket"))
                    return@addSnapshotListener
                }
                val ticket = document?.takeIf { it.exists() }?.toSupportTicket()
                trySend(ClientResult.Success(ticket, document?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }
}
