package com.example.tamisknits.features.delivery.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.dataOrNull
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveriesViewModel @Inject constructor(
    private val useCase: DeliveriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveriesUiState())
    val uiState: StateFlow<DeliveriesUiState> = _uiState

    private var ordersListener: ListenerRegistration? = null

    init {
        listenToOrders()
        viewModelScope.launch {
            useCase.getProfile().collect { result ->
                if (result is ClientResult.Success) {
                    _uiState.update { it.copy(driverName = result.dataOrNull?.name.orEmpty()) }
                }
            }
        }
    }

    private fun listenToOrders() {
        ordersListener?.remove()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        ordersListener = useCase.listenToMyOrders(
            onUpdate = { orders -> applyOrders(orders) },
            onFailure = { message ->
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        )
    }

    private fun applyOrders(orders: List<DeliveryOrder>) {
        val byTab = orders.groupBy { useCase.tabFor(it.status) }
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                toPickUp = byTab[DeliveryTab.TO_PICK_UP].orEmpty(),
                onTheWay = byTab[DeliveryTab.ON_THE_WAY].orEmpty(),
                done = byTab[DeliveryTab.DONE].orEmpty().sortedByDescending { o -> o.deliveredAt }
            )
        }
    }

    fun selectTab(tab: DeliveryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retry() = listenToOrders()

    override fun onCleared() {
        ordersListener?.remove()
        ordersListener = null
        super.onCleared()
    }
}
