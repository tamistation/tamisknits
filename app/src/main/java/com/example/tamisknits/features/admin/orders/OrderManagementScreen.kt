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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.Orders


private val Cream = Color(0xFFFDFBF9)
private val Terracotta = Color(0xFF6B1E3C)
private val TextDark = Color(0xFF1A0A10)
private val TextMuted = Color(0xFF9C8490)
private val Pending = Color(0xFFB5892A)
private val Active = Color(0xFF3D6B8C)
private val Delivered = Color(0xFF3A7D5C)
private val Cancelled = Color(0xFF8C1C1C)

@Composable
fun OrderManagementScreen(viewModel: OrderManagementViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()

    Scaffold(
        containerColor = Cream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            OrderManagementHeader()

            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )


            FilterChipRow(
                selected = uiState.selectedFilter,
                onSelect = viewModel::onFilterSelected
            )


            OrderTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::onTabSelected,
                pendingCount = viewModel.filteredOrders.collectAsState().value
                    .count { it.status == "pending" }
            )

            HorizontalDivider(color = Color(0xFFE5DDD5))


            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta)
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
                    item { Spacer(Modifier.height(8.dp)) }
                    items(orders, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            isDragging = uiState.draggingOrderId == order.orderId,
                            onDragStart = { viewModel.onDragStart(order.orderId) },
                            onDragEnd = { viewModel.onDragEnd() },
                            onAdvance = {
                                viewModel.advanceOrderStatus(
                                    order.orderId,
                                    order.status
                                )
                            },
                            onGoBack = { viewModel.goBackOrderStatus(order.orderId, order.status) },
                            onCancelClick = { viewModel.onCancelClick(order.orderId) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }


        if (uiState.cancelDialogOrderId != null) {
            CancelOrderDialog(
                uiState = uiState,
                onReasonChange = viewModel::onCancelReasonChange,
                onConfirm = viewModel::confirmCancellation,
                onDismiss = viewModel::dismissCancelDialog
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
                color = TextDark
            )
            Text(
                text = "Manage & track all orders",
                fontSize = 13.sp,
                color = TextMuted
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
                color = TextMuted,
                fontSize = 14.sp
            )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Terracotta,
            unfocusedBorderColor = Color(0xFFE0D8D0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}


@Composable
private fun FilterChipRow(selected: String, onSelect: (String) -> Unit) {
    val filters = listOf("All", "Custom", "Standard")
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
                label = { Text(filter, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Terracotta,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = TextDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == filter,
                    borderColor = Color(0xFFE0D8D0),
                    selectedBorderColor = Terracotta
                )
            )
        }
    }
}


@Composable
private fun OrderTabs(
    selectedTab: OrderTab,
    onTabSelected: (OrderTab) -> Unit,
    pendingCount: Int
) {
    val tabs = listOf(OrderTab.PENDING, OrderTab.ACTIVE, OrderTab.PREVIOUS)
    val labels = listOf("Pending", "Active", "Previous")

    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Cream,
        contentColor = Terracotta,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                color = Terracotta
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
                            color = if (selectedTab == tab) Terracotta else TextMuted,
                            fontSize = 14.sp
                        )

                        if (tab == OrderTab.PENDING && pendingCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge(containerColor = Terracotta) {
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
                        color = TextDark
                    )
                    Text(
                        text = if (order.isCustomOrder) "✦ Custom order" else "Standard order",
                        fontSize = 12.sp,
                        color = if (order.isCustomOrder) Terracotta else TextMuted
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0EBE5))
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


            StatusProgressBar(currentStatus = order.status)

            Spacer(Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                if (order.status != "cancelled") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F0EB))
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
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Drag → next status", fontSize = 12.sp, color = TextMuted)
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
                            .background(Color(0xFFFFF0EE))
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancel order",
                            tint = Cancelled,
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
    val (bg, label) = when (status) {
        "pending" -> Pending.copy(alpha = 0.15f) to Pending
        "confirmed" -> Active.copy(alpha = 0.15f) to Active
        "shipped" -> Active.copy(alpha = 0.15f) to Active
        "in_transit" -> Active.copy(alpha = 0.15f) to Active
        "delivered" -> Delivered.copy(alpha = 0.15f) to Delivered
        "cancelled" -> Cancelled.copy(alpha = 0.15f) to Cancelled
        else -> Color.LightGray.copy(alpha = 0.15f) to TextMuted
    }

    val displayLabel = status.replace("_", " ").replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = displayLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = label
        )
    }
}


@Composable
private fun StatusProgressBar(currentStatus: String) {
    val currentIndex = STATUS_FLOW.indexOf(currentStatus).coerceAtLeast(0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        STATUS_FLOW.forEachIndexed { index, status ->
            val isReached = index <= currentIndex
            val color by animateColorAsState(
                if (isReached) Terracotta else Color(0xFFE5DDD5),
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
        Text("Pending", fontSize = 9.sp, color = TextMuted)
        Text("Confirmed", fontSize = 9.sp, color = TextMuted)
        Text("Shipped", fontSize = 9.sp, color = TextMuted)
        Text("Transit", fontSize = 9.sp, color = TextMuted)
        Text("Delivered", fontSize = 9.sp, color = TextMuted)
    }
}


@Composable
private fun LabelValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun EmptyState(tab: OrderTab) {
    val (emoji, message) = when (tab) {
        OrderTab.PENDING -> "📭" to "No pending orders right now."
        OrderTab.ACTIVE -> "📦" to "No active orders at the moment."
        OrderTab.PREVIOUS -> "🗂️" to "No previous orders found."
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = TextMuted, fontSize = 15.sp)
        }
    }
}


@Composable
private fun CancelOrderDialog(
    uiState: OrderManagementUiState,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Cancel order?",
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column {
                Text(
                    "This can't be undone. Please provide a reason so the client knows why.",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.cancelReason,
                    onValueChange = onReasonChange,
                    placeholder = { Text("Reason for cancellation…", fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Color(0xFFE0D8D0)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = uiState.cancelReason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cancelled,
                    disabledContainerColor = Cancelled.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Yes, cancel order", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Go back", color = TextMuted)
            }
        }
    )
}