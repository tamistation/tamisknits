package com.example.tamisknits.models

data class SupportTickets(
    val ticketId: String = "",
    val clientId: String = "",
    val adminId: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "open",
)