package com.example.tamisknits.features.admin.userManagement.addDelivery

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDeliveryViewModel @Inject constructor(
    private val addDeliveryUseCase: AddDeliveryUseCase
) : ViewModel() {

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

    fun createDelivery() {
        val state = _uiState.value
        if (state.isCreating) return

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

            !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() -> {
                _uiState.value = state.copy(
                    error = "Please enter a valid email address"
                )
            }

            state.phone.isBlank() -> {
                _uiState.value = state.copy(
                    error = "Please enter a phone number"
                )
            }

            else -> {
                _uiState.value = state.copy(isCreating = true, error = null)
                viewModelScope.launch {
                    try {
                        val result = addDeliveryUseCase.execute(
                            AddDeliveryUseCase.Params(
                                name = state.name,
                                email = state.email,
                                phone = state.phone
                            )
                        )
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            createdEmail = result.email,
                            temporaryPassword = result.temporaryPassword
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = e.message ?: "Couldn't create the account. Please try again."
                        )
                    }
                }
            }
        }
    }
}
