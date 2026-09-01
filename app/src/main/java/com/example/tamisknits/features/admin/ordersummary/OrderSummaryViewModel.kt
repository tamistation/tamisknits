package com.example.tamisknits.features.admin.ordersummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OrderSummaryViewModel @Inject constructor(
    private val useCase: OrderSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderSummaryState())
    val uiState: StateFlow<OrderSummaryState> = _uiState

    private var loadedOrderId: String? = null

    fun loadOrder(orderId: String) {
        if (loadedOrderId == orderId) return //avoid re-fetching same order on recomposition
        loadedOrderId = orderId

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            useCase.getOrderById(
                orderId = orderId,
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(order = order)

                    viewModelScope.launch {
                        val items = useCase.getOrderItemsWithProductInfo(order.items)
                        val pricing = useCase.calculatePricing(items)
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            subtotal = pricing.subtotal,
                            deliveryFee = pricing.deliveryFee,
                            discountPercentage = pricing.discountPercentage,
                            discountAmount = pricing.discountAmount,
                            total = pricing.total
                        )
                    }
                },
                onFailure = { message ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
                }
            )
        }
    }


}
