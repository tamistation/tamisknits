package com.example.tamisknits.features.client.tracking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.StatusTracker
import com.example.tamisknits.ui.components.orderStatusDescription
import com.example.tamisknits.ui.components.orderStatusIcon
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun TrackingPage(
    viewModel: TrackingViewModel,
    onBack: () -> Unit,
    onOrderDetails: (String) -> Unit,
    onGetHelp: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            ScreenTopBar(
                title = "Track order",
                subtitle = uiState.order?.let { "Order #${it.shortId}" },
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
                    message = "We couldn't find this order.",
                    actionLabel = "Go back",
                    onAction = onBack
                )

                else -> TrackingContent(uiState, onOrderDetails, onGetHelp)
            }
        }
    }
}

@Composable
private fun TrackingContent(
    uiState: TrackingUiState,
    onOrderDetails: (String) -> Unit,
    onGetHelp: (String) -> Unit
) {
    val order = uiState.order ?: return
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
            TrackingHero(uiState)

            if (!uiState.isCancelled) {
                Spacer(Modifier.height(20.dp))
                AppCard {
                    Text("Order progress", style = AppTextStyles.Subtitle)
                    Spacer(Modifier.height(16.dp))
                    StatusTracker(uiState.steps)
                }
            }

            Spacer(Modifier.height(20.dp))
            AppCard {
                Text("Order summary", style = AppTextStyles.Subtitle)
                Spacer(Modifier.height(12.dp))
                SummaryRow("Items", "${uiState.itemCount}")
                SummaryRow("Total", ClientFormat.price(uiState.total))
                SummaryRow("Placed", ClientFormat.dateTime(order.createdAt))
                HorizontalDivider(
                    color = AppColors.OutlineSoft,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.Place,
                        contentDescription = null,
                        tint = AppColors.Terracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            uiState.shippingAddress.fullName.ifBlank { "Delivery address" },
                            style = AppTextStyles.Label
                        )
                        Text(uiState.shippingAddress.singleLine, style = AppTextStyles.BodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
                SecondaryButton(
                    text = "View order details",
                    onClick = { onOrderDetails(order.orderId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = "Contact support",
                icon = Icons.Rounded.HeadsetMic,
                onClick = { onGetHelp(order.orderId) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TrackingHero(uiState: TrackingUiState) {
    val order = uiState.order ?: return
    val (title, icon) = when {
        uiState.isCancelled -> "Order cancelled" to Icons.Rounded.Block
        uiState.isDelivered -> "Delivered!" to Icons.Rounded.Celebration
        order.status == "in_transit" -> "On its way to you" to Icons.Rounded.LocalShipping
        order.status == "shipped" -> "Packed and shipped" to orderStatusIcon(order.status)
        order.status == "confirmed" -> "Being prepared" to orderStatusIcon(order.status)
        else -> "Order received" to orderStatusIcon(order.status)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Card)
            .background(
                if (uiState.isCancelled) Brush.linearGradient(
                    listOf(
                        AppColors.TextSecondary,
                        AppColors.TextDark
                    )
                )
                else Brush.linearGradient(
                    listOf(
                        AppColors.TerracottaMuted,
                        AppColors.Terracotta,
                        AppColors.DarkTerracotta
                    )
                )
            )
            .padding(22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!uiState.isCancelled && !uiState.isDelivered) LivePulse()
                Text(
                    if (uiState.isCancelled || uiState.isDelivered) "ORDER #${order.shortId}" else "LIVE · ORDER #${order.shortId}",
                    style = AppTextStyles.Overline.copy(color = AppColors.BlushDeep)
                )
            }
            Spacer(Modifier.height(10.dp))
            AnimatedContent(
                targetState = title to icon,
                transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith fadeOut() },
                label = "heroTitle"
            ) { (heroTitle, heroIcon) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        heroIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(heroTitle, style = AppTextStyles.Title.copy(color = Color.White))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                orderStatusDescription(order.status),
                style = AppTextStyles.BodySmall.copy(color = Color.White.copy(alpha = 0.85f))
            )

            uiState.estimate?.let { estimate ->
                Spacer(Modifier.height(14.dp))
                Text(
                    "ESTIMATED DELIVERY",
                    style = AppTextStyles.Overline.copy(color = AppColors.BlushDeep)
                )
                Text(
                    ClientFormat.dayRange(estimate.earliest, estimate.latest),
                    style = AppTextStyles.Label.copy(color = Color.White)
                )
            }

            if (!uiState.isCancelled) {
                Spacer(Modifier.height(20.dp))
                DeliveryProgressBar(uiState.progress)
            }
        }
    }
}

@Composable
private fun DeliveryProgressBar(progress: Float) {
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "deliveryProgress"
    )
    val bob = rememberInfiniteTransition(label = "truckBob")
    val lift by bob.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "truckLift"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val iconSize = 32.dp
        val trackWidth = maxWidth - iconSize
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = iconSize / 2)
                .fillMaxWidth()
                .height(6.dp)
                .clip(AppShapes.Pill)
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .clip(AppShapes.Pill)
                    .background(Color.White)
            )
        }
        Box(
            modifier = Modifier
                .offset(x = trackWidth * animated.coerceIn(0f, 1f))
                .align(Alignment.CenterStart)
                .graphicsLayer { translationY = lift * density }
                .size(iconSize)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.LocalShipping,
                contentDescription = null,
                tint = AppColors.Terracotta,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LivePulse() {
    val transition = rememberInfiniteTransition(label = "livePulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "livePulseAlpha"
    )
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(8.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(Color(0xFF7CE0A3))
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = AppTextStyles.Body, modifier = Modifier.weight(1f))
        Text(value, style = AppTextStyles.Label)
    }
}

