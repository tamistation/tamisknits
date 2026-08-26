package com.example.tamisknits.features.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.Orders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//state in a separate file
//get statuses from api not viewmodel
//usecase
enum class OrderTab { PENDING, ACTIVE, PREVIOUS }

enum class OrderFilter { ALL, CUSTOM, STANDARD }

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val useCase: OrderManagementUseCase
) : ViewModel() {

    //this is private only read and write inside the class
    private val _uiState = MutableStateFlow(OrderManagementUiState())
    val uiState: StateFlow<OrderManagementUiState> = _uiState
    //this is public allowed to be read from outside class but stateflow doesnt allow write

    private val statusConfigFlow = useCase.getOrderStatusConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStatusConfig())

    //used later in advanced order status
    val statusConfig: StateFlow<OrderStatusConfig> = statusConfigFlow //public
    private val ordersFlow = useCase.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//state in converts flow to stateflow,a streaam always having a cached value
    //with 3 arguments (when to run,whent to start stop listening here 5s),initial value)


    //combine is listening to multiple flows:
    // and whenever any of these flows change filtered orders runs again
    //itt just reacts no manual refreshements
    val filteredOrders: StateFlow<List<Orders>> = combine(
        _uiState,
        ordersFlow,
        statusConfigFlow
    ) { state, orders, config ->

        //tab is filtered according to the grouping of selected configg
        val tabFiltered = when (state.selectedTab) {
            OrderTab.PENDING -> orders.filter { it.status in config.pending }
            OrderTab.ACTIVE -> orders.filter { it.status in config.active }
            OrderTab.PREVIOUS -> orders.filter { it.status in config.previous }
        }
        //also the type is according to  iscustomorder if yes or no
        val typeFiltered = when (state.selectedFilter) {
            OrderFilter.CUSTOM -> tabFiltered.filter { it.isCustomOrder }
            OrderFilter.STANDARD -> tabFiltered.filter { !it.isCustomOrder }
            OrderFilter.ALL -> tabFiltered
        }

        if (state.searchQuery.isBlank()) typeFiltered //if its blank it will
        //work like  the typefiltered
        //if not :
        else typeFiltered.filter {
            it.orderId.take(8).contains(state.searchQuery, ignoreCase = true) ||
                    it.orderId.contains(state.searchQuery, ignoreCase = true) ||
                    it.clientId.contains(state.searchQuery, ignoreCase = true) ||
                    it.status.contains(state.searchQuery, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // always counts pending orders from the FULL list, regardless of which tab/filter is active
    val pendingCount: StateFlow<Int> = combine(
        ordersFlow,
        statusConfigFlow
    ) { orders, config ->
        orders.count { it.status in config.pending }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    //executed after vm runs, collects new orders, keeps ui state updated(isloading to stop it from loading)
    init {
        viewModelScope.launch {
            ordersFlow.collect { orders ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    //get state of each
    fun onTabSelected(tab: OrderTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, searchQuery = "")
    }
    //_uistate is property of mutable statflow so it can be reassigned inside thisc lass

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterSelected(filter: OrderFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun advanceOrderStatus(orderId: String, currentStatus: String) {
        val flow = statusConfigFlow.value.flow
        val currentIndex = flow.indexOf(currentStatus) //gets indec of current status
        if (currentIndex == -1 || currentIndex >= flow.lastIndex) return //checks if its included
        //or checks if its already in last step so there is nothing to advance to
        val nextStatus = flow[currentIndex + 1] //neither is true so we advance +1 status

        useCase.updateOrderStatus(
            orderId = orderId,
            newStatus = nextStatus,//new one
            onSuccess = {},
            onFailure = {}
        )
    }

    fun goBackOrderStatus(orderId: String, currentStatus: String) {
        val flow = statusConfigFlow.value.flow
        val currentIndex = flow.indexOf(currentStatus)
        if (currentIndex <= 0) return
        //as long as current index isnt the first one we could go backk
        val prevStatus = flow[currentIndex - 1] //decrease to go back
        useCase.updateOrderStatus(
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

        useCase.updateOrderStatus(
            orderId = orderId,
            newStatus = "cancelled",
            onSuccess = { dismissCancelDialog() },
            onFailure = { dismissCancelDialog() }
        )

        useCase.updateOrder(
            orderId = orderId,
            updates = mapOf("cancellationReason" to state.cancelReason),
            onSuccess = {},
            onFailure = {}
        )
    }

    fun dismissCancelDialog() { //clearing out dialogue
        _uiState.value = _uiState.value.copy(cancelDialogOrderId = null, cancelReason = "")
    }

    //for visuals
    fun onDragStart(orderId: String) {
        _uiState.value = _uiState.value.copy(draggingOrderId = orderId)
    }

    fun onDragEnd() {
        _uiState.value = _uiState.value.copy(draggingOrderId = null)
    }
}


