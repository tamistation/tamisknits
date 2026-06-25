package com.example.tamisknits.models

data class SupportTicket(
    val ticketId: String = "",
    val clientId: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "open",
    val adminReply: String = ""
)