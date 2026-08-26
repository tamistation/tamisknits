package com.example.tamisknits.features.authentication.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.tamisknits.models.User
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppType
import com.example.tamisknits.ui.AuthenticationDivider
import com.example.tamisknits.ui.Loader
import com.example.tamisknits.ui.PasswordToggle

@Composable
fun LoginPage(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit,
    onRedirectToRegister: () -> Unit,
) {
    val loginState by viewModel.state.collectAsState()

    LoginUI(
        isLoggingIn = loginState.isLoggingIn,
        loginError = loginState.loginError,
        onLoginClick = { email, password -> viewModel.login(email, password) },
        onRegisterClick = onRedirectToRegister,
        onConsumeLoginError = viewModel::consumeLoginState,
    )

    LaunchedEffect(loginState.loggedInUser) {
        loginState.loggedInUser?.let { user ->
            onLoginSuccess(user)
            viewModel.consumeLoginState()//this ensures the navigations only happens once the moment login is confirmed
        }
    }
}

@Composable
private fun LoginUI(
    isLoggingIn: Boolean,
    loginError: String?,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    onConsumeLoginError: () -> Unit,
) {
    var missingError: String? by remember { mutableStateOf(null) }

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
                .padding(top = 40.dp),
        ) {
            AuthHeader(
                headline = "Welcome Back!",
                subhead = "Your favorite handmade pieces are waiting for you.",
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(28.dp))

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
                    .fillMaxWidth(),
            ) {
                LoginFields(
                    isLoggingIn = isLoggingIn,
                    onLogin = { email, password ->
                        if (email.isEmpty()) {
                            missingError = "Email is required"
                        } else if (password.isEmpty()) {
                            missingError = "Password is required"
                        } else {
                            onLoginClick(email, password)
                        }
                    },
                    onRegister = onRegisterClick,
                )
            }
        }

        if (isLoggingIn) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Loader()
            }
        }
    }

    missingError?.let {
        AuthDialog(onDismiss = { missingError = null }, message = it)
    }

    loginError?.let { error ->
        AuthDialog(onDismiss = onConsumeLoginError, message = error)
    }
}

@Composable
internal fun AuthHeader(
    headline: String,
    subhead: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
) {
    Column(modifier) {
        eyebrow?.let {
            Text(it.uppercase(), style = AppType.Label, color = AppColors.MarigoldGold)
            Spacer(Modifier.height(6.dp))
        }
        Text(headline, style = AppType.WordmarkItalic, color = AppColors.TextDark)
        Spacer(Modifier.height(8.dp))
        Text(subhead, style = AppType.Body, color = AppColors.TextMuted)
    }
}

@Composable
private fun LoginFields(
    isLoggingIn: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
) {
    var email: String by remember { mutableStateOf("") }
    var pass: String by remember { mutableStateOf("") }
    var isPasswordMasked: Boolean by remember { mutableStateOf(true) }

    Column {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = pass,
            onValueChange = { pass = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                PasswordToggle(
                    isPasswordMasked,
                    onToggle = { isPasswordMasked = !isPasswordMasked },
                )
            },
            visualTransformation = if (isPasswordMasked) PasswordVisualTransformation() else VisualTransformation.None,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onLogin(email, pass) },
            enabled = !isLoggingIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Terracotta,
                contentColor = AppColors.Surface,
            ),
        ) {
            Text("Log in", style = AppType.Button)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onRegister, Modifier.fillMaxWidth()) {
            Text("New here? Create an account", style = AppType.Label, color = AppColors.Terracotta)
        }
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = AppType.Label) },
        textStyle = AppType.Body.copy(color = AppColors.TextDark),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Terracotta,
            unfocusedBorderColor = AppColors.Outline,
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Blush.copy(alpha = 0.5f),
            cursorColor = AppColors.Terracotta,
            focusedLabelColor = AppColors.Terracotta,
            unfocusedLabelColor = AppColors.TextMuted,
        ),
    )
}

@Composable
internal fun AuthDialog(onDismiss: () -> Unit, message: String) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppColors.Terracotta)
            }
        },
        text = { Text(message, style = AppType.Body) },
        containerColor = AppColors.Surface,
        shape = RoundedCornerShape(20.dp),
    )
}