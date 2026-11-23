package com.example.tamisknits.features.delivery.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.delivery.common.DeliveryContact
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
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.StatusTracker
import com.example.tamisknits.ui.components.SwipeToConfirmButton
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun DeliveryDetailsPage(
    viewModel: DeliveryDetailsViewModel,
    onBack: () -> Unit,
    onDelivered: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnDelivered by rememberUpdatedState(onDelivered)

    LaunchedEffect(Unit) {
        viewModel.delivered.collect { currentOnDelivered() }
    }

    MessageEffect(
        message = uiState.message,
        hostState = snackbarHostState,
        onShown = viewModel::messageShown
    )

    Scaffold(
        containerColor = AppColors.Cream,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = uiState.order?.let { "Order #${it.order.shortId}" } ?: "Delivery",
                subtitle = uiState.order?.let { OrderStatuses.label(it.status) },
                onBack = onBack
            )
        },
        bottomBar = {
            val showActions = uiState.order != null && !uiState.notFound &&
                    !uiState.isLoading && uiState.errorMessage == null
            if (showActions) DeliveryActionBar(uiState, viewModel::onPrimaryAction)
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::retry
                )

                uiState.notFound || uiState.order == null -> EmptyView(
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = "This delivery was reassigned",
                    message = "It's no longer assigned to you.",
                    actionLabel = "Go back",
                    onAction = onBack
                )

                else -> DeliveryDetailsContent(
                    uiState = uiState,
                    onNoApp = viewModel::showMessage,
                    onReportProblem = viewModel::openReportSheet
                )
            }
        }
    }

    if (uiState.showConfirmSheet) {
        ConfirmDeliverySheet(
            uiState = uiState,
            onCashConfirmedChange = viewModel::onCashConfirmedChange,
            onConfirm = viewModel::confirmDelivery,
            onDismiss = viewModel::dismissConfirmSheet
        )
    }

    if (uiState.showReportSheet) {
        ReportProblemSheet(
            uiState = uiState,
            onReasonChange = viewModel::onIssueReasonChange,
            onOtherTextChange = viewModel::onIssueOtherTextChange,
            onSubmit = viewModel::submitIssue,
            onDismiss = viewModel::dismissReportSheet
        )
    }
}

@Composable
private fun DeliveryDetailsContent(
    uiState: DeliveryDetailsUiState,
    onNoApp: (String) -> Unit,
    onReportProblem: () -> Unit
) {
    val order = uiState.order ?: return
    val context = LocalContext.current
    val address = uiState.address

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .readableWidth()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.ItemSpacing)
    ) {
        if (order.deliveryIssue.isNotBlank()) {
            AppCard(color = AppColors.PendingSoft, elevated = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = AppColors.Pending
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Issue reported", style = AppTextStyles.Label)
                        Text(order.deliveryIssue, style = AppTextStyles.BodySmall)
                    }
                }
            }
        }

        // Customer
        AppCard {
            Text("Customer", style = AppTextStyles.Overline)
            Spacer(Modifier.height(6.dp))
            Text(address.fullName.ifBlank { "Customer" }, style = AppTextStyles.Subtitle)
            if (address.phone.isNotBlank()) {
                Text(address.phone, style = AppTextStyles.BodySmall)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = "Call",
                        icon = Icons.Rounded.Call,
                        onClick = {
                            if (!DeliveryContact.dial(context, address.phone)) {
                                onNoApp("No app found to make calls")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "WhatsApp",
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        onClick = {
                            if (!DeliveryContact.whatsApp(context, address.phone)) {
                                onNoApp("Couldn't open WhatsApp")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text("No phone number on this order", style = AppTextStyles.BodySmall)
            }
        }

        // Address
        AppCard {
            Text("Address", style = AppTextStyles.Overline)
            Spacer(Modifier.height(6.dp))
            listOf(
                "Building" to address.building,
                "Street" to address.street,
                "City" to address.city
            ).filter { it.second.isNotBlank() }.forEach { (label, value) ->
                DetailRow(label, value)
            }
            if (address.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Notes", style = AppTextStyles.Caption)
                Text(address.notes, style = AppTextStyles.Body)
            }
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = "Open in Maps",
                icon = Icons.Rounded.Map,
                onClick = {
                    if (!DeliveryContact.openInMaps(context, address)) {
                        onNoApp("Couldn't open a maps app")
                    }
                },
                enabled = DeliveryContact.mapsQuery(address).isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Items
        AppCard {
            Text(
                "Items (${order.order.itemCount})",
                style = AppTextStyles.Overline
            )
            Spacer(Modifier.height(6.dp))
            order.order.items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(
                    color = AppColors.OutlineSoft,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ItemRow(item)
            }
        }

        // Payment
        AppCard {
            Text("Payment", style = AppTextStyles.Overline)
            Spacer(Modifier.height(6.dp))
            DetailRow("Total", ClientFormat.price(uiState.total))
            DetailRow("Method", PaymentMethods.label(order.order.paymentMethod))
            HorizontalDivider(
                color = AppColors.OutlineSoft,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (uiState.isCashOnDelivery) "To collect" else "Already paid",
                    style = AppTextStyles.Label,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (uiState.isCashOnDelivery) ClientFormat.price(uiState.total) else ClientFormat.price(0.0),
                    style = AppTextStyles.Price.copy(
                        color = if (uiState.isCashOnDelivery) AppColors.Terracotta else AppColors.Delivered
                    )
                )
            }
            if (uiState.isDelivered && uiState.isCashOnDelivery) {
                Spacer(Modifier.height(8.dp))
                StatusPill(
                    label = if (order.cashCollected) "Cash collected" else "Cash not confirmed",
                    color = if (order.cashCollected) AppColors.Delivered else AppColors.Pending
                )
            }
        }

        // Progress
        if (order.status != OrderStatuses.CANCELLED && uiState.steps.isNotEmpty()) {
            AppCard {
                Text("Progress", style = AppTextStyles.Overline)
                Spacer(Modifier.height(12.dp))
                StatusTracker(uiState.steps)
            }
        }

        if (!uiState.isDelivered && order.status != OrderStatuses.CANCELLED) {
            TextButton(
                onClick = onReportProblem,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Problem with this delivery?",
                    style = AppTextStyles.Label.copy(color = AppColors.TextSecondary)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeliveryActionBar(uiState: DeliveryDetailsUiState, onAction: () -> Unit) {
    val order = uiState.order ?: return
    Surface(color = AppColors.Surface, shadowElevation = 12.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .readableWidth()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 12.dp)
        ) {
            when {
                uiState.isDelivered -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Button)
                        .background(AppColors.DeliveredSoft)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Delivered,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (order.deliveredAt > 0) "Delivered at ${ClientFormat.dateTime(order.deliveredAt)}"
                        else "Delivered",
                        style = AppTextStyles.Label.copy(color = AppColors.Delivered)
                    )
                }

                uiState.actionLabel != null -> SwipeToConfirmButton(
                    text = uiState.actionLabel,
                    onConfirm = onAction,
                    loading = uiState.isAdvancing
                )

                order.status == OrderStatuses.PENDING -> Text(
                    "Waiting for the shop to confirm this order",
                    style = AppTextStyles.BodySmall,
                    modifier = Modifier.align(Alignment.Center)
                )

                order.status == OrderStatuses.CANCELLED -> Text(
                    "This order was cancelled",
                    style = AppTextStyles.BodySmall.copy(color = AppColors.Cancelled),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.name.ifBlank { "Item" }, style = AppTextStyles.Label)
            val variant = listOf(item.color, item.size).filter { it.isNotBlank() }.joinToString(" · ")
            if (variant.isNotBlank()) Text(variant, style = AppTextStyles.BodySmall)
        }
        Text("× ${item.quantity}", style = AppTextStyles.Label)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(label, style = AppTextStyles.Body, modifier = Modifier.weight(1f))
        Text(value, style = AppTextStyles.Label)
    }
}
