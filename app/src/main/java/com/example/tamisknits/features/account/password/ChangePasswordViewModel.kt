package com.example.tamisknits.features.account.password

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val useCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState

    fun onCurrentPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                currentPassword = value,
                fieldErrors = it.fieldErrors - PasswordField.CURRENT
            )
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                fieldErrors = it.fieldErrors - PasswordField.NEW
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                fieldErrors = it.fieldErrors - PasswordField.CONFIRM
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = buildMap {
            if (state.currentPassword.isBlank()) put(
                PasswordField.CURRENT,
                "Enter your current password"
            )
            if (state.newPassword.length < MIN_PASSWORD_LENGTH) {
                put(PasswordField.NEW, "Use at least $MIN_PASSWORD_LENGTH characters")
            } else if (state.newPassword == state.currentPassword) {
                put(PasswordField.NEW, "Choose a password you haven't used here")
            }
            if (state.confirmPassword != state.newPassword) put(
                PasswordField.CONFIRM,
                "Passwords don't match"
            )
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        useCase.changePassword(
            currentPassword = state.currentPassword,
            newPassword = state.newPassword,
            onSuccess = {
                _uiState.update { ChangePasswordUiState(success = true) }
            },
            onFailure = { error ->
                val field = if (error.contains(
                        "current password",
                        ignoreCase = true
                    )
                ) PasswordField.CURRENT else null
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        fieldErrors = if (field != null) mapOf(field to error) else it.fieldErrors,
                        message = if (field == null) error else null
                    )
                }
            }
        )
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
