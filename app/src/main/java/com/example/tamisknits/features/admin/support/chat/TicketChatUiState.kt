package com.example.tamisknits.features.admin.support.chat

data class TicketChatUiState(
    val isLoading: Boolean = true,
    val clientName: String = "",
    val subject: String = "",
    val messageInput: String = "",
    val isClosing: Boolean = false,
    val sendError: String? = null,
)