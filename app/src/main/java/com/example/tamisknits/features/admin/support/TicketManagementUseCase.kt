package com.example.tamisknits.features.admin.support


import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.TicketMessage
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class TicketManagementUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {
    fun getSupportTickets(): Flow<List<SupportTickets>> = repository.getSupportTickets()

    fun getTicketMessages(ticketId: String): Flow<List<TicketMessage>> =
        repository.getTicketMessages(ticketId)

    fun updateTicketStatus(
        ticketId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = repository.updateTicketStatus(ticketId, status, onSuccess, onFailure)

    fun addTicketMessage(
        msg: TicketMessage,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = repository.addTicketMessage(msg, onSuccess, onFailure)


    suspend fun getUserName(uid: String): String {
        if (uid.isBlank()) return "Unknown"
        return suspendCancellableCoroutine { cont ->
            repository.getUser(
                uid,
                onSuccess = { user -> if (cont.isActive) cont.resume(user.name) },
                onFailure = { if (cont.isActive) cont.resume("Unknown") }
            )///since it may take time suspend is added to the call abcks so it doesnt stop other threads while laodingg
        }
    }

    suspend fun getTicket(ticketId: String): SupportTickets? =
        suspendCancellableCoroutine { cont ->
            repository.getSupportTicket(
                ticketId,
                onSuccess = { ticket -> if (cont.isActive) cont.resume(ticket) },
                onFailure = { if (cont.isActive) cont.resume(null) }
            )
        }
}