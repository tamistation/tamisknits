// new file: LoginUiState.kt
package com.example.tamisknits.features.authentication.login

import androidx.compose.runtime.Immutable
import com.example.tamisknits.models.User

@Immutable
data class LoginUiState(
    val isLoggingIn: Boolean = false,
    val loginError: String? = null,
    val loggedInUser: User? = null,
)