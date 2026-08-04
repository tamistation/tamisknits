package com.example.tamisknits.models

data class TicketMessage(
    val messageId: String = "",
    val ticketId: String = "",
    val senderId: String = "",
    val senderType: String = "",
    val message: String = "",
    val sentAt: Long = 0L
)