package com.example.tamisknits.features.client.orders.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ClientImage
import com.example.tamisknits.features.client.ui.components.ProductRowSkeleton
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.ClientOrderTab
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun OrdersPage(
    viewModel: OrdersViewModel,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onBrowseClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        topBar = { ScreenTopBar(title = "My orders", onBack = onBack) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OrderTabs(
                selected = uiState.selectedTab,
                counts = uiState.counts,
                onSelect = viewModel::onTabSelected,
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding)
            )
            OfflineBanner(uiState.isOffline)
            AnimatedContent(
                targetState = uiState.selectedTab to uiState.isLoading,
                transitionSpec = {
                    val forward = targetState.first.ordinal >= initialState.first.ordinal
                    (slideInHorizontally { if (forward) it / 6 else -it / 6 } + fadeIn()) togetherWith fadeOut()
                },
                label = "ordersTab"
            ) { (tab, loading) ->
                when {
                    loading -> Column(Modifier.padding(AppDimens.ScreenPadding)) {
                        repeat(3) { ProductRowSkeleton(Modifier.padding(vertical = 8.dp)) }
                    }

                    uiState.errorMessage != null -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    uiState.orders.isEmpty() -> when (tab) {
                        ClientOrderTab.ACTIVE -> EmptyView(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            title = "No active orders",
                            message = "When you place an order, you can follow every step of it here.",
                            actionLabel = "Start shopping",
                            onAction = onBrowseClick
                        )

                        ClientOrderTab.COMPLETED -> EmptyView(
                            icon = Icons.Rounded.CheckCircleOutline,
                            title = "No completed orders yet",
                            message = "Delivered orders will be kept here for you."
                        )

                        ClientOrderTab.CANCELLED -> EmptyView(
                            icon = Icons.Rounded.DoNotDisturbOn,
                            title = "Nothing cancelled",
                            message = "Good news: you haven't cancelled any orders."
                        )
                    }

                    else -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = AppDimens.ScreenPadding,
                            end = AppDimens.ScreenPadding,
                            top = 12.dp,
                            bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.orders, key = { it.order.orderId }) { item ->
                            OrderCard(
                                item = item,
                                onClick = { onOrderClick(item.order.orderId) },
                                modifier = Modifier
                                    .readableWidth()
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderTabs(
    selected: ClientOrderTab,
    counts: Map<ClientOrderTab, Int>,
    onSelect: (ClientOrderTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(AppShapes.Pill)
            .background(AppColors.SurfaceMuted)
            .padding(4.dp)
    ) {
        ClientOrderTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val background by animateColorAsState(
                if (isSelected) AppColors.Surface else AppColors.SurfaceMuted,
                label = "tabBackground"
            )
            val textColor by animateColorAsState(
                if (isSelected) AppColors.Terracotta else AppColors.TextSecondary,
                label = "tabText"
            )
            val count = counts[tab] ?: 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShapes.Pill)
                    .background(background)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 0) "${tab.title} ($count)" else tab.title,
                    style = AppTextStyles.LabelSmall.copy(color = textColor)
                )
            }
        }
    }
}

@Composable
private fun OrderCard(item: OrderListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val order = item.order
    val context = LocalContext.current
    AppCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Order #${order.shortId}", style = AppTextStyles.Subtitle)
                Text(ClientFormat.date(order.createdAt), style = AppTextStyles.Caption)
            }
            StatusPill(OrderStatuses.label(order.status), AppColors.orderStatusColor(order.status))
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                item.items.take(3).forEach { orderItem ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Surface)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        ClientImage(
                            model = ClientImageResolver.resolve(
                                context,
                                orderItem.imageUrl,
                                listOf(ClientImageResolver.productKey(orderItem.name))
                            ),
                            contentDescription = orderItem.name,
                            modifier = Modifier.matchParentSize(),
                            placeholderIconSize = 18.dp
                        )
                    }
                }
            }
            if (item.items.size > 3) {
                Spacer(Modifier.width(18.dp))
                Text("+${item.items.size - 3}", style = AppTextStyles.LabelSmall)
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${order.itemCount} ${if (order.itemCount == 1) "item" else "items"}",
                    style = AppTextStyles.Caption
                )
                Text(ClientFormat.price(item.total), style = AppTextStyles.Price)
            }
        }
        if (order.status !in listOf(OrderStatuses.CANCELLED, OrderStatuses.DELIVERED)) {
            Spacer(Modifier.height(14.dp))
            val progress by animateFloatAsState(
                item.progress.coerceAtLeast(0.06f),
                label = "orderProgress"
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(AppShapes.Pill),
                color = AppColors.Terracotta,
                trackColor = AppColors.Blush,
                drawStopIndicator = {}
            )
        }
    }
}

