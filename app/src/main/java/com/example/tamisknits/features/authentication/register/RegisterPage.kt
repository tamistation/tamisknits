package com.example.tamisknits.features.authentication.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.authentication.components.AuthErrorDialog
import com.example.tamisknits.features.authentication.components.AuthFooterLink
import com.example.tamisknits.features.authentication.components.AuthPasswordField
import com.example.tamisknits.features.authentication.components.AuthScaffold
import com.example.tamisknits.features.authentication.components.AuthTextField
import com.example.tamisknits.features.authentication.components.AuthValidation
import com.example.tamisknits.features.authentication.components.MIN_PASSWORD_LENGTH
import com.example.tamisknits.models.User
import com.example.tamisknits.ui.components.PrimaryButton

@Composable
fun RegisterPage(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (User) -> Unit,
    onRedirectToLogin: () -> Unit,
) {
    val registerState by viewModel.state.collectAsState()

    RegisterContent(
        isRegistering = registerState.isRegistering,
        registerError = registerState.registerError,
        onRegister = viewModel::register,
        onLogin = onRedirectToLogin,
        onDismissError = viewModel::consumeRegisterState,
    )

    LaunchedEffect(registerState.registeredUser) {
        registerState.registeredUser?.let { user ->
            onRegisterSuccess(user)
            viewModel.consumeRegisterState()
        }
    }
}

@Composable
private fun RegisterContent(
    isRegistering: Boolean,
    registerError: String?,
    onRegister: (name: String, email: String, phone: String, password: String) -> Unit,
    onLogin: () -> Unit,
    onDismissError: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        nameError = AuthValidation.name(name)
        emailError = AuthValidation.email(email)
        phoneError = AuthValidation.phone(phone)
        passwordError = AuthValidation.newPassword(password)
        confirmPasswordError = AuthValidation.confirmPassword(password, confirmPassword)

        val valid = listOf(
            nameError,
            emailError,
            phoneError,
            passwordError,
            confirmPasswordError
        ).all { it == null }
        if (valid) {
            focusManager.clearFocus()
            onRegister(name.trim(), email.trim(), phone.trim(), password)
        }
    }

    AuthScaffold(
        title = "Join the circle",
        subtitle = "Create an account to save favourites, order and track your pieces.",
        footer = {
            AuthFooterLink(
                prompt = "Already have an account?",
                action = "Log in",
                onClick = onLogin
            )
        }
    ) {
        AuthTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Full name",
            icon = Icons.Outlined.Person,
            error = nameError,
            capitalization = KeyboardCapitalization.Words
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = "Email",
            icon = Icons.Outlined.Email,
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = "Phone",
            icon = Icons.Outlined.Phone,
            error = phoneError,
            keyboardType = KeyboardType.Phone
        )
        Spacer(Modifier.height(12.dp))
        AuthPasswordField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = "Password",
            error = passwordError,
            supportingText = "At least $MIN_PASSWORD_LENGTH characters"
        )
        Spacer(Modifier.height(12.dp))
        AuthPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = "Confirm password",
            error = confirmPasswordError,
            imeAction = ImeAction.Done,
            onImeAction = { submit() }
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Create account",
            onClick = { submit() },
            loading = isRegistering,
            modifier = Modifier.fillMaxWidth()
        )
    }

    registerError?.let { AuthErrorDialog(message = it, onDismiss = onDismissError) }
}
