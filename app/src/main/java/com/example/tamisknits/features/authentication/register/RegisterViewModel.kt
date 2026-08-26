package com.example.tamisknits.features.authentication.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        userTypeName: String,
    ) {

        viewModelScope.launch {
            _state.value = _state.value.copy(isRegistering = true)
            runCatching {
                registerUseCase.execute(
                    RegisterUseCase.Params(
                        name = name,
                        email = email,
                        phone = phone,
                        password = password,
                        userTypeName = userTypeName,
                    )
                )
            }.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isRegistering = false,
                        registeredUser = user,
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isRegistering = false,
                        registerError = error.message ?: "Registration failed. Please try again.",
                    )
                }
            )
        }
    }

    fun consumeRegisterState() {
        _state.value = _state.value.copy(
            registerError = null,
            registeredUser = null,
        )
    }
}