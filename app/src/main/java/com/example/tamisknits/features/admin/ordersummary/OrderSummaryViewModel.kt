package com.example.tamisknits.features.admin.ordersummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

        loadDrivers()

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

    private fun loadDrivers() {
        _uiState.update { it.copy(isLoadingDrivers = true) }
        useCase.getDrivers(
            onSuccess = { drivers ->
                _uiState.update {
                    it.copy(drivers = drivers.sortedBy { d -> d.name.lowercase() }, isLoadingDrivers = false)
                }
            },
            onFailure = { message ->
                _uiState.update { it.copy(isLoadingDrivers = false, message = message) }
            }
        )
    }

    fun openDriverPicker() {
        val state = _uiState.value
        if (state.drivers.isEmpty() && !state.isLoadingDrivers) loadDrivers() // retry after a failed load
        _uiState.update { it.copy(showDriverPicker = true) }
    }

    fun dismissDriverPicker() {
        if (_uiState.value.isAssigningDriver) return
        _uiState.update { it.copy(showDriverPicker = false) }
    }

    fun assignDriver(driverId: String) {
        val state = _uiState.value
        val order = state.order ?: return
        if (state.isAssigningDriver) return
        if (driverId == state.assignedDriverId) {
            _uiState.update { it.copy(showDriverPicker = false) }
            return
        }

        _uiState.update { it.copy(isAssigningDriver = true) }
        useCase.assignDriver(
            orderId = order.orderId,
            driverId = driverId,
            onSuccess = {
                _uiState.update { current ->
                    val name = current.drivers.find { it.uid == driverId }?.name
                    current.copy(
                        order = current.order?.copy(deliveryId = driverId),
                        isAssigningDriver = false,
                        showDriverPicker = false,
                        message = if (name.isNullOrBlank()) "Driver assigned" else "Assigned to $name"
                    )
                }
            },
            onFailure = { message ->
                _uiState.update { it.copy(isAssigningDriver = false, message = message) }
            }
        )
    }

    fun clearDeliveryIssue() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isClearingIssue) return
        _uiState.update { it.copy(isClearingIssue = true) }
        useCase.clearDeliveryIssue(
            orderId = order.orderId,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isClearingIssue = false,
                        order = it.order?.copy(deliveryIssue = ""),
                        message = "Issue marked as resolved"
                    )
                }
            },
            onFailure = { message ->
                _uiState.update { it.copy(isClearingIssue = false, message = message) }
            }
        )
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
