package com.example.tamisknits.features.admin.support.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.admin.support.TicketManagementUseCase
import com.example.tamisknits.models.TicketMessage
import com.example.tamisknits.navigation.AdminRoute
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketChatViewModel @Inject constructor(
    private val useCase: TicketManagementUseCase,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val ticketId: String = savedStateHandle.toRoute<AdminRoute.TicketChat>().ticketId

    //identifies the ticket the admin clicked on
//it pulls the id from the nav route so app knows which convo to load
    private val _uiState = MutableStateFlow(TicketChatUiState())

    val uiState: StateFlow<TicketChatUiState> = _uiState

    val messages = useCase.getTicketMessages(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //listens to the list of messages from firestore
    init {//when the viewmodel is created it gets the ticket info,client name and updates the ui state
        viewModelScope.launch {
            val ticket = useCase.getTicket(ticketId)
            val name = ticket?.clientId?.let { useCase.getUserName(it) } ?: "Client"
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                clientName = name,
                subject = ticket?.subject ?: "",
            )
        }
    }

    fun onMessageInputChange(text: String) {
        _uiState.value = _uiState.value.copy(messageInput = text)
    }

    //updates the state to show the letter the user just typed
    fun sendMessage() {
        val text = _uiState.value.messageInput.trim()//trim removes white space
        if (text.isEmpty()) return
        val adminId = auth.currentUser?.uid ?: return

        _uiState.value = _uiState.value.copy(messageInput = "")
        useCase.addTicketMessage(
            TicketMessage(
                ticketId = ticketId,
                senderId = adminId,
                senderType = "admin",
                message = text,
            ),
            onSuccess = {},
            onFailure = { error -> _uiState.value = _uiState.value.copy(sendError = error) }
        )
    }

    fun consumeSendError() {
        _uiState.value = _uiState.value.copy(sendError = null)
    }

    fun closeTicket(onClosed: () -> Unit) {
        _uiState.value = _uiState.value.copy(isClosing = true)
        useCase.updateTicketStatus(
            ticketId,
            "done",
            onSuccess = {
                _uiState.value = _uiState.value.copy(isClosing = false)
                onClosed()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(isClosing = false, sendError = error)
            }
        )
    }
}