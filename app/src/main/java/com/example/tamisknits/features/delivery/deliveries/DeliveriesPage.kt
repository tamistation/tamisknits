package com.example.tamisknits.features.delivery.deliveries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.EmptyState
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.ShimmerBox
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.TabsWithPendingBadge
import com.example.tamisknits.ui.components.readableWidth
import java.util.Calendar

@Composable
fun DeliveriesPage(
    viewModel: DeliveriesViewModel,
    onOrderClick: (String) -> Unit,
    message: String? = null,
    onMessageShown: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    MessageEffect(message = message, hostState = snackbarHostState, onShown = onMessageShown)

    Box(
        Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {
        DeliveriesContent(uiState, viewModel, onOrderClick)
        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DeliveriesContent(
    uiState: DeliveriesUiState,
    viewModel: DeliveriesViewModel,
    onOrderClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        DeliveriesHeader(uiState)

        TabsWithPendingBadge(
            tabs = DeliveryTab.entries,
            labels = DeliveryTab.entries.map { it.title },
            selectedTab = uiState.selectedTab,
            onTabSelected = viewModel::selectTab,
            pendingTab = DeliveryTab.TO_PICK_UP,
            pendingCount = uiState.toPickUp.size
        )

        Box(Modifier.weight(1f)) {
            when {
                uiState.isLoading -> DeliveriesSkeleton()
                uiState.errorMessage != null -> ErrorView(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::retry
                )

                uiState.visibleOrders.isEmpty() -> EmptyState(
                    "🧶",
                    when (uiState.selectedTab) {
                        DeliveryTab.TO_PICK_UP -> "Nothing to pick up right now"
                        DeliveryTab.ON_THE_WAY -> "No deliveries on the way"
                        DeliveryTab.DONE -> "No completed deliveries yet"
                    }
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .readableWidth(),
                    contentPadding = PaddingValues(
                        horizontal = AppDimens.ScreenPadding,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.ItemSpacing)
                ) {
                    items(uiState.visibleOrders, key = { it.orderId }) { order ->
                        DeliveryOrderCard(order = order, onClick = { onOrderClick(order.orderId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveriesHeader(uiState: DeliveriesUiState) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val firstName = uiState.driverName.trim().substringBefore(' ')
    Column(
        modifier = Modifier
            .readableWidth()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 16.dp)
    ) {
        Text(
            if (firstName.isBlank()) "$greeting 👋" else "$greeting, $firstName 👋",
            style = AppTextStyles.Title
        )
        Spacer(Modifier.height(4.dp))
        Text(
            when {
                uiState.isLoading -> "Loading your deliveries…"
                uiState.toDeliverCount == 1 -> "1 to deliver today"
                else -> "${uiState.toDeliverCount} to deliver today"
            },
            style = AppTextStyles.BodySmall
        )
    }
}

@Composable
private fun DeliveryOrderCard(order: DeliveryOrder, onClick: () -> Unit) {
    val clientOrder = order.order
    val address = SavedAddress.fromShippingAddress(clientOrder.order.shippingAddress)
    val isCash = clientOrder.paymentMethod == PaymentMethods.CASH_ON_DELIVERY

    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Order #${clientOrder.shortId}",
                style = AppTextStyles.Overline,
                modifier = Modifier.weight(1f)
            )
            StatusPill(
                label = OrderStatuses.label(order.status),
                color = AppColors.orderStatusColor(order.status)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            address.fullName.ifBlank { "Customer" },
            style = AppTextStyles.Subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Place,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                listOf(
                    address.city.ifBlank { "No city" },
                    if (clientOrder.itemCount == 1) "1 item" else "${clientOrder.itemCount} items"
                ).joinToString(" · "),
                style = AppTextStyles.BodySmall
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when {
                    !isCash -> "Paid"
                    order.status == OrderStatuses.DELIVERED && order.cashCollected ->
                        "Collected ${ClientFormat.price(clientOrder.order.totalPrice)}"

                    else -> "Collect ${ClientFormat.price(clientOrder.order.totalPrice)}"
                },
                style = AppTextStyles.Label.copy(
                    color = if (isCash && order.status != OrderStatuses.DELIVERED) AppColors.Terracotta
                    else AppColors.Delivered
                ),
                modifier = Modifier.weight(1f)
            )
            if (order.deliveryIssue.isNotBlank()) IssueChip()
        }
    }
}

@Composable
private fun IssueChip() {
    Row(
        modifier = Modifier
            .clip(AppShapes.Pill)
            .background(AppColors.PendingSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = AppColors.Pending,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("Issue reported", style = AppTextStyles.Caption.copy(color = AppColors.Pending))
    }
}

@Composable
private fun DeliveriesSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.ItemSpacing)
    ) {
        repeat(3) {
            AppCard {
                ShimmerBox(
                    Modifier
                        .width(90.dp)
                        .height(12.dp), RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(12.dp))
                ShimmerBox(
                    Modifier
                        .width(180.dp)
                        .height(18.dp), RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(10.dp))
                ShimmerBox(
                    Modifier
                        .width(140.dp)
                        .height(12.dp), RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.height(16.dp))
                ShimmerBox(
                    Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp), RoundedCornerShape(4.dp)
                )
            }
        }
    }
}
