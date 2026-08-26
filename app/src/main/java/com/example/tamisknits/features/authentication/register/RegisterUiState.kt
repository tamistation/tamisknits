package com.example.tamisknits.features.authentication.register

import androidx.compose.runtime.Immutable
import com.example.tamisknits.models.User

@Immutable
data class RegisterUiState(
    val isRegistering: Boolean = false,
    val registerError: String? = null,
    val registeredUser: User? = null,
)