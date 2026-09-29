package com.example.tamisknits.features.admin.userManagement.addDelivery

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddDeliveryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddDeliveryUiState())
    val uiState: StateFlow<AddDeliveryUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            error = null
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            error = null
        )
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            error = null
        )
    }

    fun createDelivery(onSuccess: () -> Unit) {
        val state = _uiState.value

        when {
            state.name.isBlank() -> {
                _uiState.value = state.copy(
                    error = "Please enter a name"
                )
            }

            state.email.isBlank() -> {
                _uiState.value = state.copy(
                    error = "Please enter an email"
                )
            }

            state.phone.isBlank() -> {
                _uiState.value = state.copy(
                    error = "Please enter a phone number"
                )
            }

            else -> {
                // Firebase account creation will be added next.
                onSuccess()
            }
        }
    }
}