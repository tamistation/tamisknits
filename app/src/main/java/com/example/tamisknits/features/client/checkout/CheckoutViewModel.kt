package com.example.tamisknits.features.client.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientCartUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.models.dataOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// Fields stay null until the client touches them, so saved data (default address, profile phone)
// fills the form without overwriting anything they've typed.
private data class CheckoutControls(
    val selectedAddressId: String? = null,
    val isAddingNewAddress: Boolean = false,
    val addressForm: SavedAddress = SavedAddress(label = "Home"),
    val saveNewAddress: Boolean = true,
    val phone: String? = null,
    val paymentMethod: String = PaymentMethods.CASH_ON_DELIVERY,
    val orderNotes: String = "",
    val fieldErrors: Map<CheckoutField, String> = emptyMap(),
    val isPlacingOrder: Boolean = false,
    val placedOrderId: String? = null,
    val message: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val useCase: CheckoutUseCase,
    private val cartUseCase: ClientCartUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val controls = MutableStateFlow(CheckoutControls())

    val uiState: StateFlow<CheckoutUiState> = combine(
        cartUseCase.getCartLines(uid),
        useCase.getAddresses(uid),
        userUseCase.getProfile(),
        controls
    ) { linesResult, addressesResult, profile, control ->
        val addresses = addressesResult.dataOrNull.orEmpty()
        val selectedId = control.selectedAddressId
            ?.takeIf { id -> addresses.any { it.addressId == id } }
            ?: addresses.firstOrNull { it.isDefault }?.addressId
            ?: addresses.firstOrNull()?.addressId
        val selected = addresses.find { it.addressId == selectedId }
        val isAddingNew = control.isAddingNewAddress ||
                (addressesResult is ClientResult.Success && addresses.isEmpty())
        val profileUser = profile.dataOrNull
        val phone = control.phone
            ?: selected?.phone?.takeIf { !isAddingNew && it.isNotBlank() }
            ?: profileUser?.phone.orEmpty()
        val form = if (control.addressForm.fullName.isBlank() && profileUser != null) {
            control.addressForm.copy(fullName = profileUser.name)
        } else control.addressForm

        val base = CheckoutUiState(
            addresses = addresses,
            selectedAddressId = selectedId,
            isAddingNewAddress = isAddingNew,
            addressForm = form,
            saveNewAddress = control.saveNewAddress,
            phone = phone,
            paymentMethod = control.paymentMethod,
            orderNotes = control.orderNotes,
            fieldErrors = control.fieldErrors,
            isPlacingOrder = control.isPlacingOrder,
            placedOrderId = control.placedOrderId,
            message = control.message
        )

        when (linesResult) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = linesResult.message
            )

            is ClientResult.Success -> base.copy(
                isLoading = addressesResult is ClientResult.Loading,
                isOffline = linesResult.isFromCache,
                lines = linesResult.data,
                pricing = if (linesResult.data.isEmpty()) null else cartUseCase.pricingFor(
                    linesResult.data
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CheckoutUiState())

    fun selectAddress(addressId: String) {
        controls.update {
            it.copy(
                selectedAddressId = addressId,
                isAddingNewAddress = false,
                phone = null,
                fieldErrors = emptyMap()
            )
        }
    }

    fun startNewAddress() {
        controls.update {
            it.copy(
                isAddingNewAddress = true,
                phone = null,
                fieldErrors = emptyMap()
            )
        }
    }

    fun cancelNewAddress() {
        controls.update {
            it.copy(
                isAddingNewAddress = false,
                phone = null,
                fieldErrors = emptyMap()
            )
        }
    }

    fun onAddressFormChange(form: SavedAddress) {
        controls.update { it.copy(addressForm = form, fieldErrors = emptyMap()) }
    }

    fun onSaveNewAddressChange(save: Boolean) {
        controls.update { it.copy(saveNewAddress = save) }
    }

    fun onPhoneChange(phone: String) {
        controls.update {
            it.copy(phone = phone, fieldErrors = it.fieldErrors - CheckoutField.PHONE)
        }
    }

    fun onPaymentMethodChange(method: String) {
        controls.update { it.copy(paymentMethod = method) }
    }

    fun onOrderNotesChange(notes: String) {
        controls.update { it.copy(orderNotes = notes.take(MAX_NOTES_LENGTH)) }
    }

    fun placeOrder() {
        val state = uiState.value
        if (state.isPlacingOrder) return

        // Firestore only confirms a write once the server has it, so an offline order would sit on "placing" forever.
        if (state.isOffline) {
            controls.update { it.copy(message = "You're offline. Reconnect to place your order.") }
            return
        }
        if (state.lines.isEmpty()) {
            controls.update { it.copy(message = "Your cart is empty") }
            return
        }
        if (state.hasIssues) {
            controls.update { it.copy(message = "Some items changed. Please review your cart first.") }
            return
        }

        val address = if (state.isAddingNewAddress) state.addressForm else state.selectedAddress
        if (address == null) {
            controls.update { it.copy(message = "Please add a delivery address") }
            return
        }
        val errors = useCase.validate(address, state.phone)
        if (errors.isNotEmpty()) {
            controls.update {
                it.copy(
                    fieldErrors = errors,
                    isAddingNewAddress = state.isAddingNewAddress || errors.keys.any { key -> key != CheckoutField.PHONE })
            }
            return
        }

        controls.update { it.copy(isPlacingOrder = true, fieldErrors = emptyMap()) }

        if (state.isAddingNewAddress && state.saveNewAddress) {
            useCase.saveAddress(
                uid,
                address.copy(phone = state.phone.trim(), isDefault = state.addresses.isEmpty())
            )
        }

        useCase.placeOrder(
            clientId = uid,
            lines = state.lines,
            address = address,
            phone = state.phone,
            paymentMethod = state.paymentMethod,
            orderNotes = state.orderNotes,
            onSuccess = { orderId ->
                controls.update { it.copy(isPlacingOrder = false, placedOrderId = orderId) }
            },
            onFailure = { error ->
                controls.update { it.copy(isPlacingOrder = false, message = error) }
            }
        )
    }

    fun consumePlacedOrder() {
        controls.update { it.copy(placedOrderId = null) }
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }

    private companion object {
        const val MAX_NOTES_LENGTH = 300
    }
}
