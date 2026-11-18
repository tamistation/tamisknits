package com.example.tamisknits.features.client.support.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.features.client.support.SupportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val useCase: SupportUseCase,
    userUseCase: ClientUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val orderId = savedStateHandle.toRoute<ClientRoute.CreateTicket>().orderId

    private val _uiState = MutableStateFlow(
        if (orderId.isNullOrBlank()) CreateTicketUiState()
        else CreateTicketUiState(
            topic = TicketTopic.ORDER,
            subject = "Order #${orderId.take(8).uppercase()}"
        )
    )
    val uiState: StateFlow<CreateTicketUiState> = _uiState

    fun onTopicSelected(topic: TicketTopic) {
        _uiState.update { state ->
            val keepSubject =
                state.subject.isNotBlank() && TicketTopic.entries.none { it.label == state.subject }
            state.copy(
                topic = topic,
                subject = if (keepSubject) state.subject else topic.label,
                fieldErrors = state.fieldErrors - CreateTicketField.SUBJECT
            )
        }
    }

    fun onSubjectChange(subject: String) {
        _uiState.update {
            it.copy(
                subject = subject.take(MAX_SUBJECT_LENGTH),
                fieldErrors = it.fieldErrors - CreateTicketField.SUBJECT
            )
        }
    }

    fun onBodyChange(body: String) {
        _uiState.update {
            it.copy(
                body = body.take(MAX_BODY_LENGTH),
                fieldErrors = it.fieldErrors - CreateTicketField.BODY
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val errors = buildMap {
            if (state.subject.isBlank()) put(CreateTicketField.SUBJECT, "Add a short subject")
            if (state.body.trim().length < MIN_BODY_LENGTH) put(
                CreateTicketField.BODY,
                "Tell us a bit more so we can help"
            )
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        useCase.createTicket(
            clientId = uid,
            subject = state.subject.trim(),
            message = state.body.trim(),
            onSuccess = { ticketId ->
                _uiState.update { it.copy(isSubmitting = false, createdTicketId = ticketId) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isSubmitting = false, message = error) }
            }
        )
    }

    fun consumeCreated() {
        _uiState.update { it.copy(createdTicketId = null) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private companion object {
        const val MAX_SUBJECT_LENGTH = 80
        const val MIN_BODY_LENGTH = 10
        const val MAX_BODY_LENGTH = 1000
    }
}
