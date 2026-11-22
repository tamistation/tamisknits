package com.example.tamisknits.features.authentication.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.authentication.components.AuthErrorDialog
import com.example.tamisknits.features.authentication.components.AuthFooterLink
import com.example.tamisknits.features.authentication.components.AuthPasswordField
import com.example.tamisknits.features.authentication.components.AuthScaffold
import com.example.tamisknits.features.authentication.components.AuthTextField
import com.example.tamisknits.features.authentication.components.AuthValidation
import com.example.tamisknits.models.User
import com.example.tamisknits.ui.components.PrimaryButton

@Composable
fun LoginPage(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit,
    onRedirectToRegister: () -> Unit,
) {
    val loginState by viewModel.state.collectAsState()

    LoginContent(
        isLoggingIn = loginState.isLoggingIn,
        loginError = loginState.loginError,
        onLogin = viewModel::login,
        onRegister = onRedirectToRegister,
        onDismissError = viewModel::consumeLoginState,
    )

    LaunchedEffect(loginState.loggedInUser) {
        loginState.loggedInUser?.let { user ->
            onLoginSuccess(user)
            viewModel.consumeLoginState()
        }
    }
}

@Composable
private fun LoginContent(
    isLoggingIn: Boolean,
    loginError: String?,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
    onDismissError: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        emailError = AuthValidation.email(email)
        passwordError = AuthValidation.requiredPassword(password)
        if (emailError == null && passwordError == null) {
            focusManager.clearFocus()
            onLogin(email.trim(), password)
        }
    }

    AuthScaffold(
        title = "Welcome back",
        subtitle = "Your favourite handmade pieces are waiting for you.",
        footer = {
            AuthFooterLink(prompt = "New here?", action = "Create an account", onClick = onRegister)
        }
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = "Email",
            icon = Icons.Outlined.Email,
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthPasswordField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = "Password",
            error = passwordError,
            imeAction = ImeAction.Done,
            onImeAction = { submit() }
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Log in",
            onClick = { submit() },
            loading = isLoggingIn,
            modifier = Modifier.fillMaxWidth()
        )
    }

    loginError?.let { AuthErrorDialog(message = it, onDismiss = onDismissError) }
}
