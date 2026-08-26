package com.example.tamisknits.features.authentication.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoggingIn = true)
            runCatching {
                loginUseCase.execute(LoginUseCase.Params(email, password))
            }.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoggingIn = false,
                        loggedInUser = user,
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoggingIn = false,
                        loginError = error.message ?: "Login failed. Please try again.",
                    )
                }
            )
        }
    }

    fun consumeLoginState() {//this resets the state
        _state.value = _state.value.copy(
            loginError = null,
            loggedInUser = null,
        )
    }
}