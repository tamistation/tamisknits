package com.example.tamisknits.features.client.checkout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.cart.PriceSummary
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.AddressForm
import com.example.tamisknits.features.client.ui.components.ProductImage
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun CheckoutPage(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.placedOrderId) {
        uiState.placedOrderId?.let { orderId ->
            viewModel.consumePlacedOrder()
            onOrderPlaced(orderId)
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "Checkout", onBack = onBack) },
        bottomBar = {
            if (uiState.lines.isNotEmpty()) {
                Surface(color = AppColors.Surface, shadowElevation = 12.dp) {
                    PrimaryButton(
                        text = "Place order · ${ClientFormat.price(uiState.pricing?.total ?: 0.0)}",
                        onClick = viewModel::placeOrder,
                        loading = uiState.isPlacingOrder,
                        modifier = Modifier
                            .readableWidth()
                            .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(uiState.errorMessage.orEmpty(), onBack)
                uiState.lines.isEmpty() && !uiState.isPlacingOrder -> EmptyView(
                    icon = Icons.Rounded.ShoppingBag,
                    title = "Nothing to check out",
                    message = "Your cart is empty. Add a piece you love first.",
                    actionLabel = "Back to cart",
                    onAction = onBack
                )

                else -> CheckoutContent(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun CheckoutContent(uiState: CheckoutUiState, viewModel: CheckoutViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        OfflineBanner(uiState.isOffline)
        Column(
            modifier = Modifier
                .readableWidth()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 8.dp)
        ) {
            StepHeader(1, "Delivery address")
            AnimatedContent(
                targetState = uiState.isAddingNewAddress,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "addressMode"
            ) { addingNew ->
                if (addingNew) {
                    Column {
                        AddressForm(
                            address = uiState.addressForm,
                            onChange = viewModel::onAddressFormChange,
                            showPhone = false,
                            fullNameError = uiState.fieldErrors[CheckoutField.FULL_NAME],
                            streetError = uiState.fieldErrors[CheckoutField.STREET],
                            cityError = uiState.fieldErrors[CheckoutField.CITY]
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.onSaveNewAddressChange(!uiState.saveNewAddress) }
                        ) {
                            Checkbox(
                                checked = uiState.saveNewAddress,
                                onCheckedChange = viewModel::onSaveNewAddressChange,
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.Terracotta)
                            )
                            Text("Save this address for next time", style = AppTextStyles.Body)
                        }
                        if (uiState.addresses.isNotEmpty()) {
                            Text(
                                "Use a saved address instead",
                                style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta),
                                modifier = Modifier
                                    .clip(AppShapes.Pill)
                                    .clickable(onClick = viewModel::cancelNewAddress)
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.addresses.forEach { address ->
                            SelectableAddress(
                                address = address,
                                selected = address.addressId == uiState.selectedAddressId,
                                onClick = { viewModel.selectAddress(address.addressId) }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppShapes.CardSmall)
                                .border(1.dp, AppColors.Outline, AppShapes.CardSmall)
                                .clickable(onClick = viewModel::startNewAddress)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                tint = AppColors.Terracotta
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Add a new address",
                                style = AppTextStyles.Label.copy(color = AppColors.Terracotta)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            StepHeader(2, "Contact phone")
            AppTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone number for the courier",
                leadingIcon = Icons.Rounded.Phone,
                error = uiState.fieldErrors[CheckoutField.PHONE],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(24.dp))
            StepHeader(3, "Payment")
            PaymentOption(
                title = PaymentMethods.label(PaymentMethods.CASH_ON_DELIVERY),
                subtitle = "Pay in cash when your order arrives",
                selected = uiState.paymentMethod == PaymentMethods.CASH_ON_DELIVERY,
                onClick = { viewModel.onPaymentMethodChange(PaymentMethods.CASH_ON_DELIVERY) }
            )

            Spacer(Modifier.height(28.dp))
            StepHeader(4, "Order notes")
            AppTextField(
                value = uiState.orderNotes,
                onValueChange = viewModel::onOrderNotesChange,
                label = "Gift message or special requests (optional)",
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(Modifier.height(24.dp))
            StepHeader(5, "Review your order")
            AppCard {
                uiState.lines.forEachIndexed { index, line ->
                    SummaryLine(line)
                    if (index != uiState.lines.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }
            uiState.pricing?.let { pricing ->
                Spacer(Modifier.height(12.dp))
                PriceSummary(pricing)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepHeader(number: Int, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AppColors.Terracotta),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                style = AppTextStyles.LabelSmall.copy(color = AppColors.OnTerracotta)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(title, style = AppTextStyles.Subtitle)
    }
}

@Composable
private fun SelectableAddress(address: SavedAddress, selected: Boolean, onClick: () -> Unit) {
    val border by animateColorAsState(
        if (selected) AppColors.Terracotta else AppColors.Outline,
        label = "addressBorder"
    )
    val background by animateColorAsState(
        if (selected) AppColors.Blush.copy(alpha = 0.5f) else AppColors.Surface,
        label = "addressBackground"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(background)
            .border(if (selected) 1.5.dp else 1.dp, border, AppShapes.CardSmall)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Terracotta)
        )
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(address.label, style = AppTextStyles.Label)
                if (address.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Default",
                        style = AppTextStyles.Caption.copy(color = AppColors.Terracotta),
                        modifier = Modifier
                            .clip(AppShapes.Pill)
                            .background(AppColors.Blush)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(address.fullName, style = AppTextStyles.BodySmall)
            Text(
                address.singleLine,
                style = AppTextStyles.BodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaymentOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(if (selected) AppColors.Blush.copy(alpha = 0.5f) else AppColors.Surface)
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) AppColors.Terracotta else AppColors.Outline,
                AppShapes.CardSmall
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Payments, contentDescription = null, tint = AppColors.Terracotta)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTextStyles.Label)
            Text(subtitle, style = AppTextStyles.Caption)
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Terracotta)
        )
    }
}

@Composable
private fun SummaryLine(line: CartLine) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(52.dp)
                .clip(AppShapes.CardSmall)
        ) {
            line.product?.let {
                ProductImage(
                    it.product,
                    Modifier.matchParentSize(),
                    variant = line.variant,
                    placeholderIconSize = 20.dp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                line.name,
                style = AppTextStyles.Label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val details = listOfNotNull(
                line.variant?.color?.takeIf { it.isNotBlank() },
                line.variant?.size?.takeIf { it.isNotBlank() },
                "Qty ${line.item.quantity}"
            ).joinToString(" · ")
            Text(details, style = AppTextStyles.Caption)
        }
        Text(ClientFormat.price(line.lineTotal), style = AppTextStyles.Label)
    }
}
