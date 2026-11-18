package com.example.tamisknits.models


data class ClientTicket(
    val ticket: SupportTickets = SupportTickets(),
    val createdAt: Long = 0L
) {
    val ticketId: String get() = ticket.ticketId
    val isClosed: Boolean get() = ticket.status == TicketStatuses.DONE
}

object TicketStatuses {
    const val OPEN = "open"
    const val ACTIVE = "active"
    const val DONE = "done"

    fun label(status: String): String = when (status) {
        OPEN, "pending" -> "Waiting for reply"
        ACTIVE -> "In progress"
        DONE -> "Resolved"
        else -> status.replaceFirstChar { it.uppercase() }
    }
}
