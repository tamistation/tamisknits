package com.example.tamisknits.features.client.checkout

import com.example.tamisknits.features.pricing.PricingUseCase
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.repository.ClientAddressRepository
import com.example.tamisknits.repository.ClientOrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckoutUseCase @Inject constructor(
    private val addressRepository: ClientAddressRepository,
    private val orderRepository: ClientOrderRepository,
    private val pricingUseCase: PricingUseCase
) {
    fun getAddresses(clientId: String): Flow<ClientResult<List<SavedAddress>>> =
        addressRepository.getAddresses(clientId)

    fun saveAddress(clientId: String, address: SavedAddress) {
        addressRepository.saveAddress(clientId, address, onSuccess = {}, onFailure = {})
    }

    fun validate(address: SavedAddress, phone: String): Map<CheckoutField, String> = buildMap {
        if (address.fullName.isBlank()) put(
            CheckoutField.FULL_NAME,
            "Please enter the recipient's name"
        )
        if (!isValidPhone(phone)) put(CheckoutField.PHONE, "Please enter a valid phone number")
        if (address.street.isBlank()) put(CheckoutField.STREET, "Please enter your street address")
        if (address.city.isBlank()) put(CheckoutField.CITY, "Please enter your city")
    }

    fun placeOrder(
        clientId: String,
        lines: List<CartLine>,
        address: SavedAddress,
        phone: String,
        paymentMethod: String,
        orderNotes: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val items = lines.map { it.toOrderItem() }
        val pricing = pricingUseCase.calculatePricing(items)
        orderRepository.placeOrder(
            clientId = clientId,
            items = items,
            totalPrice = pricing.total,
            shippingAddress = address.copy(phone = phone.trim()).toShippingAddress(),
            paymentMethod = paymentMethod,
            orderNotes = orderNotes.trim(),
            cartIds = lines.map { it.item.cartId },
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun isValidPhone(phone: String): Boolean {
        val digits = phone.count { it.isDigit() }
        return digits in 7..15 && phone.all { it.isDigit() || it in "+-() " }
    }
}
