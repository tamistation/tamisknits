package com.example.tamisknits.features.admin.ordersummary



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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.User
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.Fraunces
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.StatusPill

@Composable
fun OrderSummaryPage(
    orderId: String,
    viewModel: OrderSummaryViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    MessageEffect(
        message = uiState.message,
        hostState = snackbarHostState,
        onShown = viewModel::messageShown
    )

    OrderSummaryUI(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onAssignDriverClick = viewModel::openDriverPicker,
        onDriverSelected = viewModel::assignDriver,
        onDismissDriverPicker = viewModel::dismissDriverPicker,
        onResolveIssue = viewModel::clearDeliveryIssue
    )
}

@Composable
private fun OrderSummaryUI(
    uiState: OrderSummaryState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onAssignDriverClick: () -> Unit,
    onDriverSelected: (String) -> Unit,
    onDismissDriverPicker: () -> Unit,
    onResolveIssue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextDark
                    )
                }
                PageHeader(title = "Order Summary", subtitle = "Full order details")
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Terracotta)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage, color = AppColors.TextMuted, fontSize = 15.sp)
                    }
                }
                uiState.order != null -> {
                    OrderSummaryContent(
                        uiState = uiState,
                        onAssignDriverClick = onAssignDriverClick,
                        onResolveIssue = onResolveIssue
                    )
                }
            }
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (uiState.showDriverPicker) {
        DriverPickerDialog(
            drivers = uiState.assignableDrivers,
            currentDriverId = uiState.assignedDriverId,
            isLoading = uiState.isLoadingDrivers,
            isAssigning = uiState.isAssigningDriver,
            onConfirm = onDriverSelected,
            onDismiss = onDismissDriverPicker
        )
    }
}
@Composable
private fun OrderSummaryContent(
    uiState: OrderSummaryState,
    onAssignDriverClick: () -> Unit,
    onResolveIssue: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        val issue = uiState.order?.deliveryIssue.orEmpty()
        if (issue.isNotBlank()) {
            item {
                DeliveryIssueCard(
                    issue = issue,
                    isResolving = uiState.isClearingIssue,
                    onResolve = onResolveIssue
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order #${uiState.order!!.orderId.take(8).uppercase()}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            fontFamily = Fraunces,
                            color = AppColors.TextDark
                        )
                        val (label, color) = summaryStatusPillStyle(uiState.order!!.status)
                        StatusPill(label = label, color = color)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (uiState.order!!.isCustomOrder) "✦ Custom order" else "Standard order",
                        fontSize = 13.sp,
                        color = if (uiState.order!!.isCustomOrder) AppColors.Terracotta else AppColors.TextMuted
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SummaryRow("Client", uiState.order!!.clientId)
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = AppColors.DividerLightTwo
                    )
                    SummaryRow("Total", "$${"%.2f".format(uiState.total)}")
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = AppColors.DividerLightTwo
                    )
                    SummaryRow("City", uiState.order!!.shippingAddress["city"] ?: "—")
                }
            }
        }

        item {
            DeliveryCard(
                uiState = uiState,
                onAssignDriverClick = onAssignDriverClick
            )
        }

        item {
            Text(
                "Items (${uiState.items.sumOf { it.quantity }})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextDark
            )
        }

        items(uiState.items) { orderItem ->
            OrderItemRow(orderItem)
        }


        item {
            OrderBreakdownCard(
                subtotal = uiState.subtotal,
                deliveryFee = uiState.deliveryFee,
                discountPercentage = uiState.discountPercentage,
                discountAmount = uiState.discountAmount,
                total = uiState.total
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DeliveryIssueCard(
    issue: String,
    isResolving: Boolean,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.PendingSoft),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = AppColors.Pending
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Driver reported a problem",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextDark
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(issue, fontSize = 14.sp, color = AppColors.TextDark)
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = if (isResolving) "Resolving…" else "Issue resolved",
                onClick = onResolve,
                enabled = !isResolving,
                contentColor = AppColors.Pending,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeliveryCard(
    uiState: OrderSummaryState,
    onAssignDriverClick: () -> Unit
) {
    val order = uiState.order ?: return
    val assignedDriver = uiState.assignedDriver
    val hasDriver = uiState.assignedDriverId.isNotBlank()
    val canAssign = order.status != OrderStatuses.DELIVERED && order.status != OrderStatuses.CANCELLED
    val driverLabel = when {
        !hasDriver -> "Not assigned"
        assignedDriver != null -> assignedDriver.name.ifBlank { "Unnamed driver" }
        uiState.isLoadingDrivers -> "Loading…"
        else -> "Unknown driver"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Delivery",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextDark
            )
            Spacer(Modifier.height(8.dp))
            SummaryRow("Driver", driverLabel)
            if (hasDriver) {
                assignedDriver?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Driver phone", phone)
                }
            }
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = if (hasDriver) "Change driver" else "Assign driver",
                onClick = onAssignDriverClick,
                enabled = canAssign && !uiState.isAssigningDriver,
                icon = Icons.Outlined.LocalShipping,
                modifier = Modifier.fillMaxWidth()
            )
            if (!canAssign) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Drivers can't be assigned to ${OrderStatuses.label(order.status).lowercase()} orders.",
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun DriverPickerDialog(
    drivers: List<User>,
    currentDriverId: String,
    isLoading: Boolean,
    isAssigning: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf(currentDriverId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign driver", fontWeight = FontWeight.Bold) },
        text = {
            when {
                isLoading && drivers.isEmpty() -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }

                drivers.isEmpty() -> Text(
                    "No active delivery users. Add one from User Management first.",
                    color = AppColors.TextMuted
                )

                else -> LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(drivers, key = { it.uid }) { driver ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isAssigning) { selectedId = driver.uid }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = driver.uid == selectedId,
                                onClick = { selectedId = driver.uid },
                                enabled = !isAssigning,
                                colors = RadioButtonDefaults.colors(selectedColor = AppColors.Terracotta)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    driver.name.ifBlank { "Unnamed driver" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.TextDark
                                )
                                val detail = driver.phone.ifBlank { driver.email }
                                if (detail.isNotBlank()) {
                                    Text(detail, fontSize = 12.sp, color = AppColors.TextMuted)
                                }
                            }
                            if (driver.uid == currentDriverId) {
                                Text("Current", fontSize = 12.sp, color = AppColors.TextMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedId) },
                enabled = selectedId.isNotBlank() && selectedId != currentDriverId && !isAssigning
            ) {
                Text(if (isAssigning) "Assigning…" else "Assign", color = AppColors.Terracotta)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAssigning) {
                Text("Cancel", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
private fun OrderBreakdownCard(
    subtotal: Double,
    deliveryFee: Double,
    discountPercentage: Double,
    discountAmount: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            SummaryRow("Subtotal", "$${"%.2f".format(subtotal)}")
            Spacer(Modifier.height(8.dp))
            SummaryRow("Delivery fee", "$${"%.2f".format(deliveryFee)}")
            Spacer(Modifier.height(8.dp))
            SummaryRow(
                "Discount (${discountPercentage.toInt()}%)",
                "-$${"%.2f".format(discountAmount)}"
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.DividerLightTwo)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextDark
                )
                Text(
                    "$${"%.2f".format(total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.Terracotta
                )
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ClientImageResolver.resolve(LocalContext.current, item.imageUrl),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.DragBackground)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(
                        item.size.takeIf { it.isNotBlank() },
                        item.color.takeIf { it.isNotBlank() }
                    ).joinToString(" • "),
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${item.price}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Qty ${item.quantity}",
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
    }
}

private fun summaryStatusPillStyle(status: String): Pair<String, Color> {
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