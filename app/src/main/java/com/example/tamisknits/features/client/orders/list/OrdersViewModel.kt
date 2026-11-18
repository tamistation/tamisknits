package com.example.tamisknits.features.client.orders.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.orders.OrdersUseCase
import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientOrderTab
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.progressFor
import com.example.tamisknits.models.tabFor
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val useCase: OrdersUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val selectedTab = MutableStateFlow(ClientOrderTab.ACTIVE)

    // Keyed by the raw items so an order is only re-resolved when its contents actually change,
    // not on every status update.
    private val resolvedItems =
        mutableMapOf<String, Pair<List<Map<String, Any>>, List<OrderItem>>>()

    private val statusConfig = useCase.getOrderStatusConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStatusConfig())

    private val orders: Flow<ClientResult<List<OrderListItem>>> = retryTrigger
        .flatMapLatest { useCase.getOrders(uid) }
        .mapLatest { result ->
            when (result) {
                ClientResult.Loading -> ClientResult.Loading
                is ClientResult.Error -> ClientResult.Error(result.message)
                is ClientResult.Success -> ClientResult.Success(
                    result.data.map { toListItem(it) },
                    result.isFromCache
                )
            }
        }

    val uiState: StateFlow<OrdersUiState> = combine(
        orders,
        statusConfig,
        selectedTab
    ) { ordersResult, config, tab ->
        when (ordersResult) {
            ClientResult.Loading -> OrdersUiState(selectedTab = tab)
            is ClientResult.Error -> OrdersUiState(
                isLoading = false,
                errorMessage = ordersResult.message,
                selectedTab = tab
            )

            is ClientResult.Success -> {
                val grouped = ordersResult.data.groupBy { config.tabFor(it.order.status) }
                OrdersUiState(
                    isLoading = false,
                    isOffline = ordersResult.isFromCache,
                    selectedTab = tab,
                    orders = grouped[tab].orEmpty()
                        .map { it.copy(progress = config.progressFor(it.order.status)) },
                    counts = ClientOrderTab.entries.associateWith { grouped[it]?.size ?: 0 }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrdersUiState())

    private suspend fun toListItem(order: ClientOrder): OrderListItem {
        val cached = resolvedItems[order.orderId]
        val items = if (cached != null && cached.first == order.order.items) {
            cached.second
        } else {
            useCase.resolveItems(order).also {
                resolvedItems[order.orderId] = order.order.items to it
            }
        }
        return OrderListItem(
            order = order,
            items = items,
            total = useCase.calculatePricing(items).total
        )
    }

    fun onTabSelected(tab: ClientOrderTab) {
        selectedTab.value = tab
    }

    fun retry() {
        retryTrigger.value++
    }
}
