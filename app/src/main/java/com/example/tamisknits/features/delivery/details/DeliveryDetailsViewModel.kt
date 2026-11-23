package com.example.tamisknits.features.delivery.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.delivery.navigation.DeliveryRoute
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.stepsFor
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryDetailsViewModel @Inject constructor(
    private val useCase: DeliveryDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId = savedStateHandle.toRoute<DeliveryRoute.Details>().orderId

    private val _uiState = MutableStateFlow(DeliveryDetailsUiState())
    val uiState: StateFlow<DeliveryDetailsUiState> = _uiState

    // One-off: the order was marked delivered, so the page goes back to the list.
    private val _delivered = Channel<Unit>(Channel.CONFLATED)
    val delivered: Flow<Unit> = _delivered.receiveAsFlow()

    private var orderListener: ListenerRegistration? = null
    private var statusConfig = OrderStatusConfig()

    // Status we asked for and are waiting to see. Firestore applies the write locally first, so
    // the listener confirms it right away even offline (the server ack can come much later).
    private var awaitingStatus: String? = null

    init {
        listenToOrder()
        viewModelScope.launch {
            useCase.getOrderStatusConfig().collect { config ->
                statusConfig = config
                _uiState.update { state ->
                    state.copy(steps = state.order?.let { config.stepsFor(it.status) }.orEmpty())
                }
            }
        }
    }

    private fun listenToOrder() {
        orderListener?.remove()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        orderListener = useCase.listenToOrder(
            orderId = orderId,
            onUpdate = ::applyOrder,
            onFailure = { message ->
                // Also lands here if the admin reassigns the order to someone else (read denied).
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        )
    }

    private fun applyOrder(order: DeliveryOrder?) {
        val reachedAwaited = order != null && order.status == awaitingStatus
        if (reachedAwaited) {
            if (awaitingStatus == OrderStatuses.DELIVERED) _delivered.trySend(Unit)
            awaitingStatus = null
        }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                errorMessage = null,
                notFound = order == null,
                order = order,
                steps = order?.let { statusConfig.stepsFor(it.status) }.orEmpty(),
                actionLabel = order?.let { useCase.actionLabel(it.status) },
                isAdvancing = if (reachedAwaited) false else state.isAdvancing,
                showConfirmSheet = state.showConfirmSheet && order?.status == OrderStatuses.IN_TRANSIT
            )
        }
    }

    fun retry() = listenToOrder()

    // Swipe on the bottom action. The last step goes through the confirm sheet instead.
    fun onPrimaryAction() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isAdvancing) return
        when (useCase.nextStatus(order.status)) {
            null -> Unit
            OrderStatuses.DELIVERED -> _uiState.update {
                it.copy(showConfirmSheet = true, cashConfirmed = false)
            }

            else -> advance(order, cashCollected = null)
        }
    }

    fun dismissConfirmSheet() {
        if (_uiState.value.isAdvancing) return
        _uiState.update { it.copy(showConfirmSheet = false, cashConfirmed = false) }
    }

    fun onCashConfirmedChange(checked: Boolean) {
        _uiState.update { it.copy(cashConfirmed = checked) }
    }

    fun confirmDelivery() {
        val state = _uiState.value
        val order = state.order ?: return
        if (!state.canConfirmDelivery) return
        advance(order, cashCollected = if (state.isCashOnDelivery) true else null)
    }

    private fun advance(order: DeliveryOrder, cashCollected: Boolean?) {
        val next = useCase.nextStatus(order.status) ?: return
        awaitingStatus = next
        _uiState.update { it.copy(isAdvancing = true) }
        useCase.advanceOrder(
            orderId = order.orderId,
            currentStatus = order.status,
            cashCollected = cashCollected,
            onSuccess = { newStatus ->
                if (awaitingStatus == newStatus) {
                    if (newStatus == OrderStatuses.DELIVERED) _delivered.trySend(Unit)
                    awaitingStatus = null
                }
                _uiState.update {
                    it.copy(
                        isAdvancing = false,
                        message = if (newStatus == OrderStatuses.DELIVERED) null
                        else "Updated to ${OrderStatuses.label(newStatus)}"
                    )
                }
            },
            onFailure = { message ->
                awaitingStatus = null
                _uiState.update { it.copy(isAdvancing = false, message = message) }
            }
        )
    }

    fun openReportSheet() {
        _uiState.update {
            it.copy(
                showReportSheet = true,
                issueReason = null,
                issueOtherText = "",
                issueError = null
            )
        }
    }

    fun dismissReportSheet() {
        if (_uiState.value.isReporting) return
        _uiState.update { it.copy(showReportSheet = false) }
    }

    fun onIssueReasonChange(reason: IssueReason) {
        _uiState.update { it.copy(issueReason = reason, issueError = null) }
    }

    fun onIssueOtherTextChange(text: String) {
        _uiState.update { it.copy(issueOtherText = text, issueError = null) }
    }

    fun submitIssue() {
        val state = _uiState.value
        val order = state.order ?: return
        if (state.isReporting) return
        val reason = state.issueReason
        val text = when {
            reason == null -> {
                _uiState.update { it.copy(issueError = "Choose what went wrong") }
                return
            }

            reason == IssueReason.OTHER -> state.issueOtherText.trim().ifBlank {
                _uiState.update { it.copy(issueError = "Describe the problem") }
                return
            }

            else -> reason.label
        }

        _uiState.update { it.copy(isReporting = true) }
        useCase.reportIssue(
            orderId = order.orderId,
            issue = text,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        showReportSheet = false,
                        message = "Problem reported. The shop will follow up."
                    )
                }
            },
            onFailure = { message ->
                _uiState.update { it.copy(isReporting = false, issueError = message) }
            }
        )
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onCleared() {
        orderListener?.remove()
        orderListener = null
        super.onCleared()
    }
}
