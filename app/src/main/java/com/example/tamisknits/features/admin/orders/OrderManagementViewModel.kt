package com.example.tamisknits.features.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.Orders
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


enum class OrderTab { PENDING, ACTIVE, PREVIOUS }

data class OrderManagementUiState(
    val isLoading: Boolean = true,
    val allOrders: List<Orders> = emptyList(),
    val selectedTab: OrderTab = OrderTab.PENDING,
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val cancelDialogOrderId: String? = null,
    val cancelReason: String = "",
    val draggingOrderId: String? = null
)

private val PENDING_STATUSES = listOf("pending")
private val ACTIVE_STATUSES = listOf("confirmed", "shipped", "in_transit")
private val PREVIOUS_STATUSES = listOf("delivered", "cancelled")

val STATUS_FLOW = listOf("pending", "confirmed", "shipped", "in_transit", "delivered")

class OrderManagementViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderManagementUiState())
    val uiState: StateFlow<OrderManagementUiState> = _uiState


    private val ordersFlow = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredOrders: StateFlow<List<Orders>> = combine(
        _uiState,
        ordersFlow
    ) { state, orders ->
        val tabFiltered = when (state.selectedTab) {
            OrderTab.PENDING -> orders.filter { it.status in PENDING_STATUSES }
            OrderTab.ACTIVE -> orders.filter { it.status in ACTIVE_STATUSES }
            OrderTab.PREVIOUS -> orders.filter { it.status in PREVIOUS_STATUSES }
        }
        val typeFiltered = when (state.selectedFilter) {
            "Custom" -> tabFiltered.filter { it.isCustomOrder }
            "Standard" -> tabFiltered.filter { !it.isCustomOrder }
            else -> tabFiltered
        }
        if (state.searchQuery.isBlank()) typeFiltered
        else typeFiltered.filter {
            it.orderId.take(8).contains(state.searchQuery, ignoreCase = true) ||
                    it.orderId.contains(state.searchQuery, ignoreCase = true) ||
                    it.clientId.contains(state.searchQuery, ignoreCase = true) ||
                    it.status.contains(state.searchQuery, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            ordersFlow.collect { orders ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allOrders = orders
                )
            }
        }
    }

    fun onTabSelected(tab: OrderTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, searchQuery = "")
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun advanceOrderStatus(orderId: String, currentStatus: String) {
        val currentIndex = STATUS_FLOW.indexOf(currentStatus)
        if (currentIndex == -1 || currentIndex >= STATUS_FLOW.lastIndex) return
        val nextStatus = STATUS_FLOW[currentIndex + 1]
        repository.updateOrderStatus(
            orderId = orderId,
            newStatus = nextStatus,
            onSuccess = {},
            onFailure = {}
        )
    }

    fun goBackOrderStatus(orderId: String, currentStatus: String) {
        val currentIndex = STATUS_FLOW.indexOf(currentStatus)
        if (currentIndex <= 0) return
        val prevStatus = STATUS_FLOW[currentIndex - 1]
        repository.updateOrderStatus(
            orderId = orderId,
            newStatus = prevStatus,
            onSuccess = {},
            onFailure = {}
        )
    }

    fun onCancelClick(orderId: String) {
        _uiState.value = _uiState.value.copy(cancelDialogOrderId = orderId, cancelReason = "")
    }

    fun onCancelReasonChange(reason: String) {
        _uiState.value = _uiState.value.copy(cancelReason = reason)
    }

    fun confirmCancellation() {
        val state = _uiState.value
        val orderId = state.cancelDialogOrderId ?: return
        repository.updateOrderStatus(
            orderId = orderId,
            newStatus = "cancelled",
            onSuccess = { dismissCancelDialog() },
            onFailure = { dismissCancelDialog() }
        )
        repository.updateOrder(
            orderId = orderId,
            updates = mapOf("cancellationReason" to state.cancelReason),
            onSuccess = {},
            onFailure = {}
        )
    }

    fun dismissCancelDialog() {
        _uiState.value = _uiState.value.copy(cancelDialogOrderId = null, cancelReason = "")
    }

    fun onDragStart(orderId: String) {
        _uiState.value = _uiState.value.copy(draggingOrderId = orderId)
    }

    fun onDragEnd() {
        _uiState.value = _uiState.value.copy(draggingOrderId = null)
    }
}

