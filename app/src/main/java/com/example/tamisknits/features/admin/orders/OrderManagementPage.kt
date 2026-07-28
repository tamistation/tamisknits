package com.example.tamisknits.features.admin.orders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.models.Orders
import com.example.tamisknits.theme.AppColors

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
fun OrderManagementPage(viewModel: OrderManagementViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    val statusConfig by viewModel.statusConfig.collectAsState()

//calling ui function to give it l data mnl viewmodel
    OrderManagementUI(
        uiState = uiState,
        orders = orders,
        pendingCount = orders.count { it.status == "pending" },
        statusFlow = statusConfig.flow,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onTabSelected = viewModel::onTabSelected,
        onDragStart = viewModel::onDragStart,
        onDragEnd = viewModel::onDragEnd,
        onAdvance = { orderId, status -> viewModel.advanceOrderStatus(orderId, status) },
        onGoBack = { orderId, status -> viewModel.goBackOrderStatus(orderId, status) },
        onCancelClick = viewModel::onCancelClick,
        onCancelReasonChange = viewModel::onCancelReasonChange,
        onConfirmCancellation = viewModel::confirmCancellation,
        onDismissCancelDialog = viewModel::dismissCancelDialog
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
    onDismissCancelDialog: () -> Unit
) {


    Box(
        modifier = Modifier
            .background(AppColors.Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)

        ) {

            OrderManagementHeader()

            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange
            )


            FilterChipRow(
                selected = uiState.selectedFilter,
                onSelect = onFilterSelected
            )


            OrderTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected,
                pendingCount = pendingCount
            )

            HorizontalDivider(color = AppColors.DividerLight)



            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }
            } else if (orders.isEmpty()) {
                EmptyState(tab = uiState.selectedTab)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                    items(orders, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            isDragging = uiState.draggingOrderId == order.orderId,
                            statusFlow = statusFlow,
                            onDragStart = { onDragStart(order.orderId) },
                            onDragEnd = { onDragEnd() },
                            onAdvance = {
                                onAdvance(order.orderId, order.status)
                            },
                            onGoBack = { onGoBack(order.orderId, order.status) },
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
    }

}

@Composable
private fun OrderManagementHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Orders",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextDark
            )
            Text(
                text = "Manage & track all orders",
                fontSize = 13.sp,
                color = AppColors.TextMuted
            )
        }
    }
}


@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),


        placeholder = {
            Text(
                "Search by order ID or client…",
                color = AppColors.TextMuted,
                fontSize = 14.sp
            )
        },

        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = AppColors.TextMuted
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Terracotta,
            unfocusedBorderColor = AppColors.Outline,
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface
        )
    )
}


@Composable
private fun FilterChipRow(selected: OrderFilter, onSelect: (OrderFilter) -> Unit) {
    val filters = listOf(OrderFilter.ALL, OrderFilter.CUSTOM, OrderFilter.STANDARD)
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
private fun OrderTabs( //Ai
    selectedTab: OrderTab,
    onTabSelected: (OrderTab) -> Unit,
    pendingCount: Int
) {
    val tabs = listOf(OrderTab.PENDING, OrderTab.ACTIVE, OrderTab.PREVIOUS)
    val labels = listOf("Pending", "Active", "Previous")

    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = AppColors.Cream,
        contentColor = AppColors.Terracotta,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                color = AppColors.Terracotta
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = labels[index],
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == tab) AppColors.Terracotta else AppColors.TextMuted,
                            fontSize = 14.sp
                        )

                        if (tab == OrderTab.PENDING && pendingCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge(containerColor = AppColors.Terracotta) {
                                Text("$pendingCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun OrderCard(
    order: Orders,
    isDragging: Boolean,
    statusFlow: List<String>,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onAdvance: () -> Unit,
    onGoBack: () -> Unit,
    onCancelClick: () -> Unit
) {

    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "scale")

    val cardElevation by animateFloatAsState(if (isDragging) 8f else 2f, label = "elevation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(cardElevation.dp, RoundedCornerShape(16.dp)),
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
                StatusBadge(status = order.status)
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
                LabelValue("Total", "$${order.totalPrice}")
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
//Ai
                if (order.status != "cancelled") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.DragBackground)

                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .pointerInput(order.orderId) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { onDragStart() },
                                    onDrag = { _, dragAmount ->
                                        dragOffsetX += dragAmount.x

                                        if (dragOffsetX > 40f) {
                                            onAdvance()
                                            dragOffsetX = 0f
                                        }
                                        if (dragOffsetX < -40f) {
                                            onGoBack()
                                            dragOffsetX = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        dragOffsetX = 0f
                                        onDragEnd()
                                    },
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                        onDragEnd()
                                    }
                                )

                            }
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Drag to advance status",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Drag → next status", fontSize = 12.sp, color = AppColors.TextMuted)
                    }
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


@Composable
private fun StatusBadge(status: String) {
    val statusColor = when (status) {
        "pending" -> AppColors.Pending
        "confirmed", "shipped", "in_transit" -> AppColors.Active
        "delivered" -> AppColors.Delivered
        "cancelled" -> AppColors.Cancelled
        else -> AppColors.UnknownStatus
    }

    val displayWord = status.replace("_", " ").replaceFirstChar { it.uppercase() }
//coming from firestore
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(statusColor.copy(alpha = 0.15f))

            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = displayWord,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
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