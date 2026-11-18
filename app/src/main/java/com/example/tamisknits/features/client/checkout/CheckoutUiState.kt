package com.example.tamisknits.features.client.checkout

import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.OrderPricingDto
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.SavedAddress

enum class CheckoutField { FULL_NAME, PHONE, STREET, CITY }

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val lines: List<CartLine> = emptyList(),
    val pricing: OrderPricingDto? = null,
    val addresses: List<SavedAddress> = emptyList(),
    val selectedAddressId: String? = null,
    val isAddingNewAddress: Boolean = false,
    val addressForm: SavedAddress = SavedAddress(),
    val saveNewAddress: Boolean = true,
    val phone: String = "",
    val paymentMethod: String = PaymentMethods.CASH_ON_DELIVERY,
    val orderNotes: String = "",
    val fieldErrors: Map<CheckoutField, String> = emptyMap(),
    val isPlacingOrder: Boolean = false,
    val placedOrderId: String? = null,
    val message: String? = null
) {
    val selectedAddress: SavedAddress?
        get() = addresses.find { it.addressId == selectedAddressId }

    val hasIssues: Boolean
        get() = lines.any { !it.isAvailable || it.exceedsStock }

    val itemCount: Int
        get() = lines.sumOf { it.item.quantity }
}
