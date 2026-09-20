package com.example.tamisknits.features.authentication.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.authentication.login.AuthDialog
import com.example.tamisknits.features.authentication.login.AuthHeader
import com.example.tamisknits.features.authentication.login.AuthTextField
import com.example.tamisknits.models.User
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppType
import com.example.tamisknits.ui.AuthenticationDivider
import com.example.tamisknits.ui.Loader
import com.example.tamisknits.ui.PasswordToggle

@Composable
fun RegisterPage(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (User) -> Unit,
    onRedirectToLogin: () -> Unit,
) {
    val registerState by viewModel.state.collectAsState()

    RegisterUI(
        isRegistering = registerState.isRegistering,
        registerError = registerState.registerError,
        onRegisterClick = { name, email, phone, password ->
            viewModel.register(name, email, phone, password)
        },
        onLoginClick = onRedirectToLogin,
        onConsumeRegisterError = viewModel::consumeRegisterState,
    )

    LaunchedEffect(registerState.registeredUser) {
        registerState.registeredUser?.let { user ->
            onRegisterSuccess(user)
            viewModel.consumeRegisterState()
        }
    }
}

@Composable
private fun RegisterUI(
    isRegistering: Boolean,
    registerError: String?,
    onRegisterClick: (name: String, email: String, phone: String, password: String) -> Unit,
    onLoginClick: () -> Unit,
    onConsumeRegisterError: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(AppColors.Cream),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top + WindowInsetsSides.Bottom
                    )
                )
                .padding(top = 30.dp),
        ) {
            AuthHeader(
                headline = "Join the circle",
                subhead = "Tell us a bit about you to get started.",
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(24.dp))

            AuthenticationDivider(
                modifier = Modifier.padding(horizontal = 15.dp),
                color = AppColors.Terracotta,
            )

            Column(
                Modifier
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .background(
                        AppColors.Surface,
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    )
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                RegisterFields(
                    isRegistering = isRegistering,
                    // Field-level validation happens inside RegisterFields (inline errors).
                    // By the time this fires, every field has already passed its own check.
                    onRegister = { name, email, phone, password, _ ->
                        onRegisterClick(name, email, phone, password)
                    },
                    onLogin = onLoginClick,
                )
            }
        }

        if (isRegistering) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Loader()
            }
        }
    }

    // Reserved for real submission failures (email already registered, network error) —
    // not for missing/invalid fields, which are shown inline where they occur.
    registerError?.let { error ->
        AuthDialog(onDismiss = onConsumeRegisterError, message = error)
    }
}

@Composable
private fun RegisterFields(
    isRegistering: Boolean,
    onRegister: (
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
    ) -> Unit,
    onLogin: () -> Unit,
) {
    var name: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }
    var phone: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    var confirmPassword: String by remember { mutableStateOf("") }
    var isPasswordMasked: Boolean by remember { mutableStateOf(true) }
    var isConfirmPasswordMasked: Boolean by remember { mutableStateOf(true) }

    var nameError: String? by remember { mutableStateOf(null) }
    var emailError: String? by remember { mutableStateOf(null) }
    var phoneError: String? by remember { mutableStateOf(null) }
    var passwordError: String? by remember { mutableStateOf(null) }
    var confirmPasswordError: String? by remember { mutableStateOf(null) }

    Column {
        Spacer(Modifier.height(20.dp))

        AuthTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Name",
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = AppColors.TextMuted)
            },
            errorText = nameError,
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = "Email",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = AppColors.TextMuted)
            },
            errorText = emailError,
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = "Phone",
            keyboardType = KeyboardType.Phone,
            leadingIcon = {
                Icon(Icons.Outlined.Phone, contentDescription = null, tint = AppColors.TextMuted)
            },
            errorText = phoneError,
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = "Password",
            keyboardType = KeyboardType.Password,
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = AppColors.TextMuted)
            },
            trailingIcon = {
                PasswordToggle(
                    isPasswordMasked,
                    onToggle = { isPasswordMasked = !isPasswordMasked },
                )
            },
            visualTransformation = if (isPasswordMasked) PasswordVisualTransformation() else VisualTransformation.None,
            errorText = passwordError,
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = AppColors.TextMuted)
            },
            trailingIcon = {
                PasswordToggle(
                    isConfirmPasswordMasked,
                    onToggle = { isConfirmPasswordMasked = !isConfirmPasswordMasked },
                )
            },
            visualTransformation = if (isConfirmPasswordMasked) PasswordVisualTransformation() else VisualTransformation.None,
            errorText = confirmPasswordError,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                nameError = if (name.isBlank()) "Enter your name" else null
                emailError = if (email.isBlank()) "Enter your email" else null
                phoneError = if (phone.isBlank()) "Enter your phone number" else null
                passwordError = when {
                    password.isBlank() -> "Enter a password"
                    password.length < 6 -> "Use at least 6 characters"
                    else -> null
                }
                confirmPasswordError = when {
                    confirmPassword.isBlank() -> "Confirm your password"
                    confirmPassword != password -> "Passwords don't match"
                    else -> null
                }

                val hasError = listOf(
                    nameError, emailError, phoneError, passwordError, confirmPasswordError
                ).any { it != null }

                if (!hasError) {
                    onRegister(name, email, phone, password, confirmPassword)
                }
            },
            enabled = !isRegistering,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Terracotta,
                contentColor = AppColors.Surface,
            ),
        ) {
            Text("Create account", style = AppType.Button)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogin,
            Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
        ) {
            Text("Back to login", style = AppType.Label, color = AppColors.Terracotta)
        }
    }
}