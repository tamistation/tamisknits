package com.example.tamisknits.features.admin.support.list


enum class TicketTab { PENDING, ACTIVE, DONE }

data class TicketManagementUiState(
    val isLoading: Boolean = true,
    val selectedTab: TicketTab = TicketTab.PENDING,
    val searchQuery: String = "",
    val draggingTicketId: String? = null
)