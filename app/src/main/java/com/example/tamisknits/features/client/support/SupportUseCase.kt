package com.example.tamisknits.features.client.support

import com.example.tamisknits.features.client.data.repository.ClientSupportRepository
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.ClientTicket
import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.TicketMessage
import com.example.tamisknits.models.TicketStatuses
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SupportUseCase @Inject constructor(
    private val supportRepository: ClientSupportRepository,
    private val repository: FirebaseRepository
) {
    fun getTickets(clientId: String): Flow<ClientResult<List<ClientTicket>>> =
        supportRepository.getTickets(clientId)

    fun getTicket(ticketId: String): Flow<ClientResult<SupportTickets?>> =
        supportRepository.getTicket(ticketId)

    fun getTicketMessages(ticketId: String): Flow<List<TicketMessage>> =
        repository.getTicketMessages(ticketId)

    // Same document shape the admin support list reads; "open" lands in its pending tab.
    fun createTicket(
        clientId: String,
        subject: String,
        message: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = repository.addSupportTicket(
        SupportTickets(
            clientId = clientId,
            subject = subject,
            message = message,
            status = TicketStatuses.OPEN
        ),
        onSuccess,
        onFailure
    )

    fun sendMessage(
        ticketId: String,
        clientId: String,
        text: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = repository.addTicketMessage(
        TicketMessage(
            ticketId = ticketId,
            senderId = clientId,
            senderType = SENDER_CLIENT,
            message = text
        ),
        onSuccess,
        onFailure
    )

    companion object {
        const val SENDER_CLIENT = "client"
        const val SENDER_ADMIN = "admin"
    }
}
