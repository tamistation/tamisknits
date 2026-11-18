package com.example.tamisknits.features.client.support.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.features.client.support.SupportUseCase
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.TicketMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class ChatControls(
    val messageInput: String = "",
    val sendError: String? = null
)

@HiltViewModel
class SupportChatViewModel @Inject constructor(
    private val useCase: SupportUseCase,
    userUseCase: ClientUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticketId = savedStateHandle.toRoute<ClientRoute.SupportChat>().ticketId
    val currentUid = userUseCase.currentUid
    private val controls = MutableStateFlow(ChatControls())

    val messages: StateFlow<List<TicketMessage>> = useCase.getTicketMessages(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // The ticket is live so the input locks as soon as the admin marks it done.
    val uiState: StateFlow<SupportChatUiState> = combine(
        useCase.getTicket(ticketId),
        controls
    ) { ticketResult, control ->
        val base =
            SupportChatUiState(messageInput = control.messageInput, sendError = control.sendError)
        when (ticketResult) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = ticketResult.message
            )

            is ClientResult.Success -> {
                val ticket = ticketResult.data
                if (ticket == null) base.copy(isLoading = false, notFound = true)
                else base.copy(isLoading = false, ticket = ticket)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupportChatUiState())

    fun onMessageInputChange(text: String) {
        controls.update { it.copy(messageInput = text.take(MAX_MESSAGE_LENGTH)) }
    }

    fun sendMessage() {
        val text = controls.value.messageInput.trim()
        if (text.isEmpty() || uiState.value.isClosed || currentUid.isBlank()) return

        controls.update { it.copy(messageInput = "") }
        useCase.sendMessage(
            ticketId = ticketId,
            clientId = currentUid,
            text = text,
            onSuccess = {},
            onFailure = { error ->
                controls.update { it.copy(sendError = error, messageInput = text) }
            }
        )
    }

    fun consumeSendError() {
        controls.update { it.copy(sendError = null) }
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }
}
