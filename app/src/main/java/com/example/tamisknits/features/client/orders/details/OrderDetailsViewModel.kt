package com.example.tamisknits.features.client.orders.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.features.client.orders.OrdersUseCase
import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.DeliveryEstimate
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.models.canClientCancel
import com.example.tamisknits.models.stepsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class OrderDetailsControls(
    val showCancelDialog: Boolean = false,
    val cancelReason: String = "",
    val isCancelling: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val useCase: OrdersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId = savedStateHandle.toRoute<ClientRoute.OrderDetails>().orderId
    private val retryTrigger = MutableStateFlow(0)
    private val controls = MutableStateFlow(OrderDetailsControls())

    private val statusConfig = useCase.getOrderStatusConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStatusConfig())

    private val order: Flow<ClientResult<Pair<ClientOrder, List<OrderItem>>?>> = retryTrigger
        .flatMapLatest { useCase.getOrder(orderId) }
        .mapLatest { result ->
            when (result) {
                ClientResult.Loading -> ClientResult.Loading
                is ClientResult.Error -> ClientResult.Error(result.message)
                is ClientResult.Success -> ClientResult.Success(
                    result.data?.let { it to useCase.resolveItems(it) },
                    result.isFromCache
                )
            }
        }

    val uiState: StateFlow<OrderDetailsUiState> = combine(
        order,
        statusConfig,
        controls
    ) { orderResult, config, control ->
        val base = OrderDetailsUiState(
            showCancelDialog = control.showCancelDialog,
            cancelReason = control.cancelReason,
            isCancelling = control.isCancelling,
            message = control.message
        )
        when (orderResult) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = orderResult.message
            )

            is ClientResult.Success -> {
                val data = orderResult.data
                if (data == null) {
                    base.copy(isLoading = false, notFound = true)
                } else {
                    val (clientOrder, items) = data
                    val status = clientOrder.status
                    base.copy(
                        isLoading = false,
                        isOffline = orderResult.isFromCache,
                        order = clientOrder,
                        items = items,
                        pricing = useCase.calculatePricing(items),
                        shippingAddress = SavedAddress.fromShippingAddress(clientOrder.order.shippingAddress),
                        steps = if (status == OrderStatuses.CANCELLED) emptyList() else config.stepsFor(
                            status
                        ),
                        estimate = if (status in config.previous) null else DeliveryEstimate.from(
                            clientOrder
                        ),
                        canCancel = config.canClientCancel(status)
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderDetailsUiState())

    fun retry() {
        retryTrigger.value++
    }

    fun onCancelClick() {
        controls.update { it.copy(showCancelDialog = true, cancelReason = "") }
    }

    fun onCancelReasonChange(reason: String) {
        controls.update { it.copy(cancelReason = reason.take(MAX_REASON_LENGTH)) }
    }

    fun dismissCancelDialog() {
        controls.update { it.copy(showCancelDialog = false, cancelReason = "") }
    }

    // Re-checked here because the admin may have confirmed the order while the dialog was open.
    fun confirmCancellation() {
        val state = uiState.value
        if (!state.canCancel) {
            controls.update {
                it.copy(
                    showCancelDialog = false,
                    message = "This order is already being prepared and can't be cancelled"
                )
            }
            return
        }
        controls.update { it.copy(isCancelling = true) }
        useCase.cancelOrder(
            orderId = orderId,
            reason = state.cancelReason.trim().ifBlank { "Cancelled by customer" },
            onSuccess = {
                controls.update {
                    it.copy(
                        isCancelling = false,
                        showCancelDialog = false,
                        message = "Your order was cancelled"
                    )
                }
            },
            onFailure = { error ->
                controls.update { it.copy(isCancelling = false, message = error) }
            }
        )
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }

    private companion object {
        const val MAX_REASON_LENGTH = 200
    }
}
