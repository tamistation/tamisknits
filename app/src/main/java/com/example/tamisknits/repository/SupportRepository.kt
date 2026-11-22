package com.example.tamisknits.repository

import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.TicketMessage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class SupportRepository(
    private val firestore: FirebaseFirestore
) {


    // add ticket
    fun addSupportTicket(
        ticket: SupportTickets,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "clientId" to ticket.clientId,
            "adminId" to ticket.adminId,
            "subject" to ticket.subject,
            "message" to ticket.message,
            "status" to ticket.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("SupportTickets").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add ticket") }
    }

    // get all tickets
    fun getSupportTickets(): Flow<List<SupportTickets>> = callbackFlow {
        val listener = firestore.collection("SupportTickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    SupportTickets(
                        ticketId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        adminId = doc.getString("adminId") ?: "",
                        subject = doc.getString("subject") ?: "",
                        message = doc.getString("message") ?: "",
                        status = doc.getString("status") ?: "open"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // edit ticket status
    fun updateTicketStatus(
        ticketId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("SupportTickets").document(ticketId)
            .update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update ticket") }
    }

    // delete ticket
    fun deleteSupportTicket(ticketId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("SupportTickets").document(ticketId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete ticket") }
    }


    // add request
    fun addCustomizationRequest(
        request: CustomizationRequest,
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
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("CustomizationRequests").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add request") }
    }

    // get all requests
    fun getCustomizationRequests(): Flow<List<CustomizationRequest>> = callbackFlow {
        val listener = firestore.collection("CustomizationRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    CustomizationRequest(
                        requestId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        adminId = doc.getString("adminId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        details = doc.getString("details") ?: "",
                        colorPreference = doc.getString("colorPreference") ?: "",
                        sizePreference = doc.getString("sizePreference") ?: "",
                        status = doc.getString("status") ?: "pending"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // edit request status
    fun updateCustomizationStatus(
        requestId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("CustomizationRequests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update request") }
    }

    // delete request
    fun deleteCustomizationRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("CustomizationRequests").document(requestId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete request") }
    }

    // get tickets by client
    fun getTicketsByClient(clientId: String): Flow<List<SupportTickets>> = callbackFlow {
        val listener = firestore.collection("SupportTickets")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    SupportTickets(
                        ticketId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        adminId = doc.getString("adminId") ?: "",
                        subject = doc.getString("subject") ?: "",
                        message = doc.getString("message") ?: "",
                        status = doc.getString("status") ?: "open"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // add a chat message to a ticket
    fun addTicketMessage(
        msg: TicketMessage,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "ticketId" to msg.ticketId,
            "senderId" to msg.senderId,
            "senderType" to msg.senderType,
            "message" to msg.message,
            "sentAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("TicketMessages").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to send message") }
    }

    // live stream of messages for a ticket, ordered by time
    fun getTicketMessages(ticketId: String): Flow<List<TicketMessage>> = callbackFlow {
        val listener = firestore.collection("TicketMessages")
            .whereEqualTo("ticketId", ticketId)
            .orderBy("sentAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TicketMessages", "Query failed", error) // add this line
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    TicketMessage(
                        messageId = doc.id,
                        ticketId = doc.getString("ticketId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        senderType = doc.getString("senderType") ?: "",
                        message = doc.getString("message") ?: "",
                        sentAt = doc.getTimestamp("sentAt")?.seconds ?: 0L
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }


    }

    fun getSupportTicket(
        ticketId: String,
        onSuccess: (SupportTickets) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("SupportTickets").document(ticketId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        SupportTickets(
                            ticketId = doc.id,
                            clientId = doc.getString("clientId") ?: "",
                            adminId = doc.getString("adminId") ?: "",
                            subject = doc.getString("subject") ?: "",
                            message = doc.getString("message") ?: "",
                            status = doc.getString("status") ?: "open"
                        )
                    )
                } else onFailure("Ticket not found")
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get ticket") }
    }
}
