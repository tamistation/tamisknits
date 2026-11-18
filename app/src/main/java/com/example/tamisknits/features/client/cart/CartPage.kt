package com.example.tamisknits.features.client.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import com.example.tamisknits.features.client.ui.components.ProductImage
import com.example.tamisknits.features.client.ui.components.ProductRowSkeleton
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.OrderPricingDto
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.QuantityStepper
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.contentStateOf
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun CartPage(
    viewModel: CartViewModel,
    onCheckout: () -> Unit,
    onProductClick: (String) -> Unit,
    onBrowseClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(
        message = uiState.message,
        hostState = snackbarHostState,
        onShown = viewModel::consumeMessage,
        actionLabel = if (uiState.lastRemoved != null) "Undo" else null,
        onAction = viewModel::undoRemove
    )

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = "Your cart",
                subtitle = if (uiState.itemCount > 0) {
                    "${uiState.itemCount} ${if (uiState.itemCount == 1) "item" else "items"}"
                } else null
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.lines.isNotEmpty() && uiState.pricing != null,
                enter = slideInVertically { it } + fadeIn()
            ) {
                CheckoutBar(
                    total = uiState.pricing?.total ?: 0.0,
                    canCheckout = uiState.canCheckout,
                    hasIssues = uiState.hasIssues,
                    onCheckout = onCheckout
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OfflineBanner(uiState.isOffline)
            Crossfade(
                targetState = contentStateOf(
                    uiState.isLoading,
                    uiState.errorMessage,
                    uiState.isEmpty
                ),
                label = "cartState"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> Column(Modifier.padding(AppDimens.ScreenPadding)) {
                        repeat(3) { ProductRowSkeleton(Modifier.padding(vertical = 6.dp)) }
                    }

                    ContentState.ERROR -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    ContentState.EMPTY -> EmptyView(
                        icon = Icons.Rounded.ShoppingBag,
                        title = "Your cart is empty",
                        message = "Every bag is handmade in small batches. Find one that feels like you.",
                        actionLabel = "Start shopping",
                        onAction = onBrowseClick
                    )

                    ContentState.CONTENT -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = AppDimens.ScreenPadding,
                            end = AppDimens.ScreenPadding,
                            top = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.lines, key = { it.item.cartId }) { line ->
                            SwipeToRemove(
                                onRemove = { viewModel.remove(line) },
                                modifier = Modifier
                                    .readableWidth()
                                    .animateItem()
                            ) {
                                CartLineCard(
                                    line = line,
                                    isUpdating = line.item.cartId in uiState.updatingCartIds,
                                    onClick = { onProductClick(line.item.productId) },
                                    onIncrease = { viewModel.increase(line) },
                                    onDecrease = { viewModel.decrease(line) },
                                    onRemove = { viewModel.remove(line) },
                                    onFix = { viewModel.fixQuantity(line) }
                                )
                            }
                        }
                        uiState.pricing?.let { pricing ->
                            item(key = "summary") {
                                PriceSummary(pricing, Modifier
                                    .readableWidth()
                                    .padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (state.targetValue == SwipeToDismissBoxValue.EndToStart) AppColors.Cancelled else AppColors.CancelButtonBackground,
                label = "swipeBackground"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppShapes.Card)
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Remove",
                    tint = AppColors.OnTerracotta
                )
            }
        }
    ) {
        content()
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    isUpdating: Boolean,
    onClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onFix: () -> Unit
) {
    AppCard(contentPadding = PaddingValues(12.dp)) {
        Row {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(AppShapes.CardSmall)
                    .clickable(onClick = onClick)
            ) {
                val product = line.product
                if (product != null) {
                    ProductImage(
                        product.product,
                        Modifier.matchParentSize(),
                        variant = line.variant,
                        placeholderIconSize = 28.dp
                    )
                } else {
                    Box(Modifier
                        .matchParentSize()
                        .background(AppColors.SurfaceMuted))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = line.name,
                        style = AppTextStyles.Label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove ${line.name}",
                        tint = AppColors.TextMuted,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRemove)
                            .padding(4.dp)
                    )
                }
                val variantText = listOfNotNull(
                    line.variant?.color?.takeIf { it.isNotBlank() },
                    line.variant?.size?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (variantText.isNotBlank()) {
                    Text(variantText, style = AppTextStyles.Caption)
                }
                Spacer(Modifier.height(8.dp))
                when {
                    !line.isAvailable -> IssueRow("No longer available", "Remove", onRemove)
                    line.exceedsStock -> IssueRow(
                        "Only ${line.availableStock} left",
                        "Update",
                        onFix
                    )

                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ClientFormat.price(line.lineTotal),
                            style = AppTextStyles.Price,
                            modifier = Modifier.weight(1f)
                        )
                        QuantityStepper(
                            quantity = line.item.quantity,
                            onDecrease = onDecrease,
                            onIncrease = onIncrease,
                            canDecrease = line.item.quantity > 1 && !isUpdating,
                            canIncrease = line.item.quantity < line.availableStock && !isUpdating,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueRow(message: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(AppColors.PendingSoft)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = AppColors.Pending,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            message,
            style = AppTextStyles.Caption.copy(color = AppColors.TextDark),
            modifier = Modifier.weight(1f)
        )
        Text(
            actionLabel,
            style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta),
            modifier = Modifier
                .clip(AppShapes.Pill)
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriceSummary(pricing: OrderPricingDto, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, color = AppColors.SurfaceMuted, elevated = false) {
        PriceRow("Subtotal", ClientFormat.price(pricing.subtotal))
        if (pricing.discountAmount > 0) {
            PriceRow(
                "Discount (${pricing.discountPercentage.toInt()}%)",
                "-${ClientFormat.price(pricing.discountAmount)}"
            )
        }
        PriceRow("Delivery", ClientFormat.price(pricing.deliveryFee))
        HorizontalDivider(color = AppColors.Outline, modifier = Modifier.padding(vertical = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = AppTextStyles.Subtitle, modifier = Modifier.weight(1f))
            Text(
                ClientFormat.price(pricing.total),
                style = AppTextStyles.Title.copy(color = AppColors.Terracotta)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.LocalShipping,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Cash on delivery available", style = AppTextStyles.Caption)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = AppTextStyles.Body, modifier = Modifier.weight(1f))
        Text(value, style = AppTextStyles.Label)
    }
}

@Composable
private fun CheckoutBar(
    total: Double,
    canCheckout: Boolean,
    hasIssues: Boolean,
    onCheckout: () -> Unit
) {
    Surface(color = AppColors.Surface, shadowElevation = 12.dp) {
        Column(
            modifier = Modifier
                .readableWidth()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp)
        ) {
            if (hasIssues) {
                Text(
                    "Please update the highlighted items before checking out.",
                    style = AppTextStyles.Caption.copy(color = AppColors.Pending),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total", style = AppTextStyles.Caption)
                    Text(
                        ClientFormat.price(total),
                        style = AppTextStyles.Title.copy(color = AppColors.Terracotta)
                    )
                }
                PrimaryButton(
                    text = "Checkout",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onCheckout,
                    enabled = canCheckout,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}
