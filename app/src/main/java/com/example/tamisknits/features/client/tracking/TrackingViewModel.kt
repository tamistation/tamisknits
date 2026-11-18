package com.example.tamisknits.features.client.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.features.client.orders.OrdersUseCase
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.DeliveryEstimate
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.models.progressFor
import com.example.tamisknits.models.stepsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

// Driven by the same Orders.status the admin advances, through the live status flow in config/orderStatuses.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val useCase: OrdersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId = savedStateHandle.toRoute<ClientRoute.OrderTracking>().orderId
    private val retryTrigger = MutableStateFlow(0)

    private val statusConfig = useCase.getOrderStatusConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStatusConfig())

    val uiState: StateFlow<TrackingUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getOrder(orderId) }
            .mapLatest { result ->
                when (result) {
                    ClientResult.Loading -> ClientResult.Loading
                    is ClientResult.Error -> ClientResult.Error(result.message)
                    is ClientResult.Success -> ClientResult.Success(
                        result.data?.let { it to useCase.resolveItems(it) },
                        result.isFromCache
                    )
                }
            },
        statusConfig
    ) { orderResult, config ->
        when (orderResult) {
            ClientResult.Loading -> TrackingUiState()
            is ClientResult.Error -> TrackingUiState(
                isLoading = false,
                errorMessage = orderResult.message
            )

            is ClientResult.Success -> {
                val data = orderResult.data
                if (data == null) {
                    TrackingUiState(isLoading = false, notFound = true)
                } else {
                    val (order, items) = data
                    val status = order.status
                    val isCancelled = status == OrderStatuses.CANCELLED
                    val isDelivered = status == config.flow.lastOrNull()
                    TrackingUiState(
                        isLoading = false,
                        isOffline = orderResult.isFromCache,
                        order = order,
                        items = items,
                        total = useCase.calculatePricing(items).total,
                        steps = config.stepsFor(status),
                        progress = if (isCancelled) 0f else config.progressFor(status),
                        estimate = if (isCancelled || isDelivered) null else DeliveryEstimate.from(
                            order
                        ),
                        shippingAddress = SavedAddress.fromShippingAddress(order.order.shippingAddress),
                        isCancelled = isCancelled,
                        isDelivered = isDelivered
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrackingUiState())

    fun retry() {
        retryTrigger.value++
    }
}
