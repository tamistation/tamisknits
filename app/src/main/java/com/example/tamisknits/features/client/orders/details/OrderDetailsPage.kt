package com.example.tamisknits.features.client.orders.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.features.client.cart.PriceSummary
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ClientImage
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.StatusTracker
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun OrderDetailsPage(
    viewModel: OrderDetailsViewModel,
    onBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onGetHelp: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = uiState.order?.let { "Order #${it.shortId}" } ?: "Order",
                subtitle = uiState.order?.let { "Placed ${ClientFormat.dateTime(it.createdAt)}" },
                onBack = onBack
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(
                    uiState.errorMessage.orEmpty(),
                    viewModel::retry
                )

                uiState.notFound || uiState.order == null -> EmptyView(
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = "Order not found",
                    message = "We couldn't find this order. It may have been removed.",
                    actionLabel = "Go back",
                    onAction = onBack
                )

                else -> OrderDetailsContent(
                    uiState,
                    viewModel,
                    onTrackOrder,
                    onProductClick,
                    onGetHelp
                )
            }
        }
    }

    if (uiState.showCancelDialog) {
        ConfirmationDialog(
            title = "Cancel this order?",
            message = "We haven't started on it yet, so you can still cancel. Let us know why (optional).",
            confirmLabel = if (uiState.isCancelling) "Cancelling…" else "Cancel order",
            confirmColor = AppColors.Cancelled,
            inputValue = uiState.cancelReason,
            inputPlaceholder = "Reason for cancelling",
            onInputChange = viewModel::onCancelReasonChange,
            confirmEnabled = !uiState.isCancelling,
            onConfirm = viewModel::confirmCancellation,
            onDismiss = viewModel::dismissCancelDialog
        )
    }
}

@Composable
private fun OrderDetailsContent(
    uiState: OrderDetailsUiState,
    viewModel: OrderDetailsViewModel,
    onTrackOrder: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onGetHelp: (String) -> Unit
) {
    val order = uiState.order ?: return
    val isCancelled = order.status == OrderStatuses.CANCELLED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OfflineBanner(uiState.isOffline)
        Column(
            modifier = Modifier
                .readableWidth()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 8.dp)
        ) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status", style = AppTextStyles.Subtitle, modifier = Modifier.weight(1f))
                    StatusPill(
                        OrderStatuses.label(order.status),
                        AppColors.orderStatusColor(order.status)
                    )
                }
                if (isCancelled) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardSmall)
                            .background(AppColors.CancelButtonBackground)
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Rounded.Block,
                            contentDescription = null,
                            tint = AppColors.Cancelled,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("This order was cancelled", style = AppTextStyles.Label)
                            if (order.cancellationReason.isNotBlank()) {
                                Text(order.cancellationReason, style = AppTextStyles.BodySmall)
                            }
                        }
                    }
                } else {
                    uiState.estimate?.let { estimate ->
                        Spacer(Modifier.height(10.dp))
                        InfoLine(
                            Icons.Rounded.CalendarMonth,
                            "Estimated delivery: ${
                                ClientFormat.dayRange(
                                    estimate.earliest,
                                    estimate.latest
                                )
                            }"
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    StatusTracker(uiState.steps)
                    if (order.status != OrderStatuses.DELIVERED) {
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = "Track live",
                            icon = Icons.Rounded.LocalShipping,
                            onClick = { onTrackOrder(order.orderId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SectionTitle("Items")
            AppCard {
                uiState.items.forEachIndexed { index, item ->
                    OrderItemRow(item, onClick = { onProductClick(item.productId) })
                    if (index != uiState.items.lastIndex) {
                        HorizontalDivider(
                            color = AppColors.OutlineSoft,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            SectionTitle("Delivery")
            AppCard {
                val address = uiState.shippingAddress
                InfoLine(
                    Icons.Rounded.Place,
                    listOf(address.fullName, address.singleLine).filter { it.isNotBlank() }
                        .joinToString("\n").ifBlank { "No address on file" })
                if (address.phone.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        address.phone,
                        style = AppTextStyles.BodySmall,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
                if (address.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        address.notes,
                        style = AppTextStyles.Caption,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                InfoLine(Icons.Rounded.Payments, PaymentMethods.label(order.paymentMethod))
                if (order.orderNotes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Your note", style = AppTextStyles.LabelSmall)
                    Text(order.orderNotes, style = AppTextStyles.BodySmall)
                }
            }

            uiState.pricing?.let { pricing ->
                SectionTitle("Payment summary")
                PriceSummary(pricing)
            }

            Spacer(Modifier.height(20.dp))
            SecondaryButton(
                text = "Need help with this order?",
                icon = Icons.Rounded.HeadsetMic,
                onClick = { onGetHelp(order.orderId) },
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.canCancel) {
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = "Cancel order",
                    icon = Icons.Rounded.Block,
                    onClick = viewModel::onCancelClick,
                    contentColor = AppColors.Cancelled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = AppTextStyles.Subtitle,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
    )
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            tint = AppColors.Terracotta,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = AppTextStyles.Body.copy(color = AppColors.TextDark))
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val model = remember(item.imageUrl, item.name) {
        ClientImageResolver.resolve(
            context,
            item.imageUrl,
            listOf(ClientImageResolver.productKey(item.name))
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClientImage(
            model = model,
            contentDescription = item.name,
            modifier = Modifier
                .size(60.dp)
                .clip(AppShapes.CardSmall),
            placeholderIconSize = 22.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                style = AppTextStyles.Label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val details =
                listOf(item.color, item.size).filter { it.isNotBlank() } + "Qty ${item.quantity}"
            Text(details.joinToString(" · "), style = AppTextStyles.Caption)
        }
        Text(ClientFormat.price(item.price * item.quantity), style = AppTextStyles.Label)
    }
}
