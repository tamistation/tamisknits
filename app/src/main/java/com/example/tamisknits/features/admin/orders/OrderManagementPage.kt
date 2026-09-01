package com.example.tamisknits.features.admin.orders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.models.Orders
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.AppSearchBar
import com.example.tamisknits.ui.components.DragToAdvanceRow
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.TabsWithPendingBadge
import kotlinx.coroutines.launch

//removed colors and put them in a separate file
//turned screen to page
//divided it to page and ui
//page has the viewmodel
//variables are in ui,then sent to page
//changed scaffold to box
//some functions khlyton ai like the drag and drop
//tested the dragn drop
//dialog in a separate file

@Composable
fun OrderManagementPage(
    viewModel: OrderManagementViewModel,
    onOrderClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    val statusConfig by viewModel.statusConfig.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
//the viewmodel exposes its data as a flow(stream of values) collectAsstate() converts it to a state
//so whenever viewmodel updates the data ,this screen automatically redraws new values

//calling ui function to give it l data mnl viewmodel
    OrderManagementUI(
        uiState = uiState,
        orders = orders,
        pendingCount = pendingCount,
        statusFlow = statusConfig.flow,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        //That :: is shorthand in Kotlin for "pass this function itself as a value,
        onFilterSelected = viewModel::onFilterSelected,
        onTabSelected = viewModel::onTabSelected,
        onDragStart = viewModel::onDragStart,
        onDragEnd = viewModel::onDragEnd,
        onAdvance = { orderId, status -> viewModel.advanceOrderStatus(orderId, status) },
        onGoBack = { orderId, status -> viewModel.goBackOrderStatus(orderId, status) },
        onCancelClick = viewModel::onCancelClick,
        onCancelReasonChange = viewModel::onCancelReasonChange,
        onConfirmCancellation = viewModel::confirmCancellation,
        onDismissCancelDialog = viewModel::dismissCancelDialog,
        onOrderClick = onOrderClick
    )

}

@Composable
private fun OrderManagementUI(// hol declarations to fill in data fo2 with types
    uiState: OrderManagementUiState,
    orders: List<Orders>,
    pendingCount: Int,
    statusFlow: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (OrderFilter) -> Unit,
    onTabSelected: (OrderTab) -> Unit,
    onDragStart: (String) -> Unit,
    onDragEnd: () -> Unit,
    onAdvance: (String, String) -> Unit,
    onGoBack: (String, String) -> Unit,
    onCancelClick: (String) -> Unit,
    onCancelReasonChange: (String) -> Unit,
    onConfirmCancellation: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val previousStatuses = remember { mutableMapOf<String, String>() }

    Box(
        modifier = Modifier
            .background(AppColors.Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)

        ) {

            PageHeader(title = "Orders", subtitle = "Manage & track all orders")

            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search by order ID or client…",
            )


            FilterChipRow(
                selected = uiState.selectedFilter,
                onSelect = onFilterSelected
            )


            TabsWithPendingBadge(
                tabs = listOf(OrderTab.PENDING, OrderTab.ACTIVE, OrderTab.PREVIOUS),
                labels = listOf("Pending", "Active", "Previous"),
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected,
                pendingTab = OrderTab.PENDING,
                pendingCount = pendingCount,
            )
            HorizontalDivider(color = AppColors.DividerLight)



            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }//spinner
            } else if (orders.isEmpty()) {
                EmptyState(tab = uiState.selectedTab)
            } else {
                LazyColumn( //scrollable list
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                    items(orders, key = { it.orderId }) { order ->
                        val currentIndex = statusFlow.indexOf(order.status)

                        OrderCard(
                            order = order,
                            calculatedTotal = uiState.calculatedTotals[order.orderId] ?: 0.0,
                            isDragging = uiState.draggingOrderId == order.orderId,
                            statusFlow = statusFlow,
                            onDragStart = { onDragStart(order.orderId) },
                            onDragEnd = { onDragEnd() },
                            onAdvance = {
                                val nextStatus = statusFlow.getOrNull(currentIndex + 1)
                                if (nextStatus != null) {
                                    val label = nextStatus.replace("_", " ")
                                        .replaceFirstChar { it.uppercase() }
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Order moved to $label")
                                    }
                                    val newTab = statusToTab(nextStatus)
                                    if (newTab != uiState.selectedTab) {
                                        onTabSelected(newTab)
                                    }
                                }
                                onAdvance(order.orderId, order.status)
                            },
                            onGoBack = {
                                val prevStatus = statusFlow.getOrNull(currentIndex - 1)
                                if (prevStatus != null) {
                                    val label = prevStatus.replace("_", " ")
                                        .replaceFirstChar { it.uppercase() }
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Order moved to $label")
                                    }
                                    val newTab = statusToTab(prevStatus)
                                    if (newTab != uiState.selectedTab) {
                                        onTabSelected(newTab)
                                    }
                                }
                                onGoBack(order.orderId, order.status)
                            },
                            onClick = { onOrderClick(order.orderId) },
                            onCancelClick = { onCancelClick(order.orderId) }

                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }


        if (uiState.cancelDialogOrderId != null) {
            CancelOrderDialog(
                uiState = uiState,
                onReasonChange = onCancelReasonChange,
                onConfirm = onConfirmCancellation,
                onDismiss = onDismissCancelDialog
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                containerColor = AppColors.Terracotta,
                contentColor = Color.White
            ) {
                Text(data.visuals.message)
            }
        }
    }

}

@Composable
private fun FilterChipRow(selected: OrderFilter, onSelect: (OrderFilter) -> Unit) {
    val filters = listOf(OrderFilter.ALL, OrderFilter.CUSTOM, OrderFilter.STANDARD)
    //enum class

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label(), fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Terracotta,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = AppColors.TextDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == filter,
                    borderColor = AppColors.Outline,
                    selectedBorderColor = AppColors.Terracotta
                )
            )
        }
    }
}

fun OrderFilter.label(): String = when (this) {
    OrderFilter.ALL -> "All"
    OrderFilter.CUSTOM -> "Custom"
    OrderFilter.STANDARD -> "Standard"
}



@Composable
private fun OrderCard(
    order: Orders,
    calculatedTotal: Double,
    isDragging: Boolean,
    statusFlow: List<String>,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onAdvance: () -> Unit,
    onGoBack: () -> Unit,
    onCancelClick: () -> Unit,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "scale")
    val cardElevation by animateFloatAsState(if (isDragging) 8f else 2f, label = "elevation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(cardElevation.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId.take(8).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextDark
                    )
                    Text(
                        text = if (order.isCustomOrder) "✦ Custom order" else "Standard order",
                        fontSize = 12.sp,
                        color = if (order.isCustomOrder) AppColors.Terracotta else AppColors.TextMuted
                    )
                }
                val (label, color) = orderStatusPillStyle(order.status)
                StatusPill(label = label, color = color)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = AppColors.DividerLightTwo)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelValue("Client", order.clientId.take(10))
                LabelValue("Items", "${order.items.size}")
                LabelValue(
                    "Total",
                    "$${"%.2f".format(calculatedTotal)}"
                )

                LabelValue("City", order.shippingAddress["city"] ?: "—")
            }

            Spacer(Modifier.height(12.dp))

            StatusProgressBar(currentStatus = order.status, statusFlow = statusFlow)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (order.status != "cancelled") {
                    DragToAdvanceRow(
                        itemKey = order.orderId,
                        onAdvance = onAdvance,
                        onGoBack = onGoBack,
                        onDragStart = onDragStart,
                        onDragEnd = onDragEnd
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (order.status != "cancelled") {
                    IconButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppColors.CancelButtonBackground)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancel order",
                            tint = AppColors.Cancelled,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


private fun orderStatusPillStyle(status: String): Pair<String, Color> {
    val color = when (status) {
        "pending" -> AppColors.Pending
        "confirmed", "shipped", "in_transit" -> AppColors.Active
        "delivered" -> AppColors.Delivered
        "cancelled" -> AppColors.Cancelled
        else -> AppColors.UnknownStatus
    }
    val label = status.replace("_", " ").replaceFirstChar { it.uppercase() }
    return label to color
}

private fun statusToTab(status: String): OrderTab = when (status) {
    "pending" -> OrderTab.PENDING
    "confirmed", "shipped", "in_transit" -> OrderTab.ACTIVE
    "delivered", "cancelled" -> OrderTab.PREVIOUS
    else -> OrderTab.PENDING
}


@Composable
private fun StatusProgressBar(currentStatus: String, statusFlow: List<String>) {
    val currentIndex =
        statusFlow.indexOf(currentStatus).coerceAtLeast(0)//so it doesnt become negative
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        statusFlow.forEachIndexed { index, status ->
            val isReached = index <= currentIndex
            val color by animateColorAsState(
                if (isReached) AppColors.Terracotta else AppColors.DividerLight,
                label = "progress_$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

        }
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        statusFlow.forEach { status ->
            val label = status.replace("_", " ").replaceFirstChar { it.uppercase() }
            Text(label, fontSize = 9.sp, color = AppColors.TextMuted)
        }
    }
}


@Composable
private fun LabelValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = AppColors.TextMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextDark

        )
    }
}


@Composable
private fun EmptyState(tab: OrderTab) {

    val (emoji, message) = when (tab) {
        OrderTab.PENDING -> "📭" to "No pending orders "
        OrderTab.ACTIVE -> "📦" to "No active orders "
        OrderTab.PREVIOUS -> "🗂️" to "No previous orders found."
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(emoji, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = AppColors.TextMuted, fontSize = 15.sp)
        }
    }
}


@Composable
fun CancelOrderDialog(
    uiState: OrderManagementUiState,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = "Cancel order?",
        message = "This can't be undone. Please provide a reason so the client knows why.",
        confirmLabel = "Yes, cancel order",
        confirmColor = AppColors.Cancelled,
        inputValue = uiState.cancelReason,
        inputPlaceholder = "Reason for cancellation…",
        onInputChange = onReasonChange,
        confirmEnabled = uiState.cancelReason.isNotBlank(),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}