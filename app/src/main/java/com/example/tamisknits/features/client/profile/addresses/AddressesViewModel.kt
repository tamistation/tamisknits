package com.example.tamisknits.features.client.profile.addresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.profile.ProfileUseCase
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class AddressesControls(
    val editor: SavedAddress? = null,
    val editorErrors: Map<AddressField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val confirmDeleteId: String? = null,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddressesViewModel @Inject constructor(
    private val useCase: ProfileUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val controls = MutableStateFlow(AddressesControls())

    val uiState: StateFlow<AddressesUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getAddresses(uid) },
        controls
    ) { addresses, control ->
        val base = AddressesUiState(
            editor = control.editor,
            editorErrors = control.editorErrors,
            isSaving = control.isSaving,
            confirmDeleteId = control.confirmDeleteId,
            message = control.message
        )
        when (addresses) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(isLoading = false, errorMessage = addresses.message)
            is ClientResult.Success -> base.copy(
                isLoading = false,
                isOffline = addresses.isFromCache,
                addresses = addresses.data
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddressesUiState())

    fun retry() {
        retryTrigger.value++
    }

    fun addAddress() {
        val isFirst = uiState.value.addresses.isEmpty()
        controls.update {
            it.copy(
                editor = SavedAddress(isDefault = isFirst),
                editorErrors = emptyMap()
            )
        }
    }

    fun editAddress(address: SavedAddress) {
        controls.update { it.copy(editor = address, editorErrors = emptyMap()) }
    }

    fun onEditorChange(address: SavedAddress) {
        controls.update { it.copy(editor = address, editorErrors = emptyMap()) }
    }

    fun dismissEditor() {
        controls.update { it.copy(editor = null, editorErrors = emptyMap()) }
    }

    fun saveEditor() {
        val address = controls.value.editor ?: return
        val errors = buildMap {
            if (address.fullName.isBlank()) put(AddressField.FULL_NAME, "Please enter a name")
            if (!useCase.isValidPhone(address.phone)) put(
                AddressField.PHONE,
                "Please enter a valid phone number"
            )
            if (address.street.isBlank()) put(AddressField.STREET, "Please enter the street")
            if (address.city.isBlank()) put(AddressField.CITY, "Please enter the city")
        }
        if (errors.isNotEmpty()) {
            controls.update { it.copy(editorErrors = errors) }
            return
        }

        controls.update { it.copy(isSaving = true) }
        useCase.saveAddress(
            clientId = uid,
            address = address.copy(
                label = address.label.trim().ifBlank { "Home" },
                fullName = address.fullName.trim(),
                phone = address.phone.trim(),
                street = address.street.trim(),
                building = address.building.trim(),
                city = address.city.trim(),
                notes = address.notes.trim()
            ),
            onSuccess = {
                controls.update {
                    it.copy(
                        isSaving = false,
                        editor = null,
                        message = "Address saved"
                    )
                }
            },
            onFailure = { error ->
                controls.update { it.copy(isSaving = false, message = error) }
            }
        )
    }

    fun setDefault(addressId: String) {
        useCase.setDefaultAddress(
            clientId = uid,
            addressId = addressId,
            onSuccess = {},
            onFailure = { error -> controls.update { it.copy(message = error) } }
        )
    }

    fun requestDelete(addressId: String) {
        controls.update { it.copy(confirmDeleteId = addressId) }
    }

    fun dismissDelete() {
        controls.update { it.copy(confirmDeleteId = null) }
    }

    fun confirmDelete() {
        val addressId = controls.value.confirmDeleteId ?: return
        controls.update { it.copy(confirmDeleteId = null) }
        useCase.deleteAddress(
            clientId = uid,
            addressId = addressId,
            onSuccess = { controls.update { it.copy(message = "Address removed") } },
            onFailure = { error -> controls.update { it.copy(message = error) } }
        )
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }
}
