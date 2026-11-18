package com.example.tamisknits.features.client.support.list

import com.example.tamisknits.models.ClientTicket

data class SupportTicketsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val openTickets: List<ClientTicket> = emptyList(),
    val closedTickets: List<ClientTicket> = emptyList()
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && openTickets.isEmpty() && closedTickets.isEmpty()
}
