package com.example.tamisknits.features.account.password

enum class PasswordField { CURRENT, NEW, CONFIRM }

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val fieldErrors: Map<PasswordField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val message: String? = null
)
