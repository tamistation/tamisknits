package com.example.tamisknits.features.client.support.chat

import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.TicketStatuses

data class SupportChatUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notFound: Boolean = false,
    val ticket: SupportTickets? = null,
    val messageInput: String = "",
    val sendError: String? = null
) {
    val isClosed: Boolean
        get() = ticket?.status == TicketStatuses.DONE
}
