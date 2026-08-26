package com.example.tamisknits.features.admin.support.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.admin.support.TicketManagementUseCase
import com.example.tamisknits.models.SupportTickets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketManagementViewModel @Inject constructor(
    private val useCase: TicketManagementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketManagementUiState())
    val uiState: StateFlow<TicketManagementUiState> = _uiState

    private val ticketsFlow = useCase.getSupportTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _clientNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val clientNames: StateFlow<Map<String, String>> = _clientNames.asStateFlow()

    init {
        viewModelScope.launch {
            ticketsFlow.collect { tickets ->
                _uiState.value = _uiState.value.copy(isLoading = false)

                val missingIds = tickets.map { it.clientId }
                    .distinct()
                    .filter { it.isNotBlank() && it !in _clientNames.value }

                missingIds.forEach { clientId ->
                    launch {
                        val name = useCase.getUserName(clientId)
                        _clientNames.value = _clientNames.value + (clientId to name)
                    }
                }
            }
        }
    }

    val filteredTickets: StateFlow<List<SupportTickets>> = combine(
        _uiState,
        ticketsFlow,
        _clientNames
    ) { state, tickets, names ->
        val tabFiltered = when (state.selectedTab) {

            TicketTab.PENDING -> tickets.filter { it.status == "pending" }
            TicketTab.ACTIVE -> tickets.filter { it.status == "active" }
            TicketTab.DONE -> tickets.filter { it.status == "done" }
        }

        if (state.searchQuery.isBlank()) tabFiltered
        else tabFiltered.filter { ticket ->
            ticket.subject.contains(state.searchQuery, ignoreCase = true) ||
                    ticket.message.contains(state.searchQuery, ignoreCase = true) ||
                    (names[ticket.clientId] ?: "").contains(state.searchQuery, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = ticketsFlow
        .map { tickets -> tickets.count { it.status == "pending" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onTabSelected(tab: TicketTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, searchQuery = "")
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onDragStart(ticketId: String) {
        _uiState.update { it.copy(draggingTicketId = ticketId) }
    }

    fun onDragEnd() {
        _uiState.update { it.copy(draggingTicketId = null) }
    }

    fun advanceStatus(ticketId: String, currentStatus: String) {
        val next = when (currentStatus) {
            "pending" -> "active"
            "active" -> "done"
            else -> return
        }
        useCase.updateTicketStatus(ticketId, next, onSuccess = {}, onFailure = {})
    }

    fun goBackStatus(ticketId: String, currentStatus: String) {
        val prev = when (currentStatus) {
            "done" -> "active"
            "active" -> "pending"
            else -> return
        }
        useCase.updateTicketStatus(ticketId, prev, onSuccess = {}, onFailure = {})
    }

    fun closeTicket(ticketId: String) {
        useCase.updateTicketStatus(ticketId, "done", onSuccess = {}, onFailure = {})
    }
}