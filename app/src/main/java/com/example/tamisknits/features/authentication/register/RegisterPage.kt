package com.example.tamisknits.features.authentication.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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


private data class RoleOption(val id: String, val label: String, val color: Color)

private val roleOptions = listOf(
    RoleOption("client", "Client", AppColors.Terracotta),
    RoleOption("delivery", "Delivery Partner", AppColors.Terracotta),
)

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
        onRegisterClick = { name, email, phone, password, userType ->
            viewModel.register(name, email, phone, password, userType)
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
    onRegisterClick: (name: String, email: String, phone: String, password: String, userType: String) -> Unit,
    onLoginClick: () -> Unit,
    onConsumeRegisterError: () -> Unit,
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
                    onRegister = { name, email, phone, password, confirmPassword, userType ->
                        when {
                            name.isEmpty() -> missingError = "Name is required"
                            email.isEmpty() -> missingError = "Email is required"
                            phone.isEmpty() -> missingError = "Phone is required"
                            password.isEmpty() -> missingError = "Password is required"
                            password.length < 6 -> missingError =
                                "Password must be at least 6 characters"

                            password != confirmPassword -> missingError = "Passwords do not match"
                            else -> onRegisterClick(name, email, phone, password, userType)
                        }
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

    missingError?.let {
        AuthDialog(onDismiss = { missingError = null }, message = it)
    }

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
        userType: String,
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
    var selectedRoleIndex: Int by remember { mutableIntStateOf(0) }

    Column {
        Spacer(Modifier.height(8.dp))
        RoleSelector(
            selectedIndex = selectedRoleIndex,
            onSelect = { selectedRoleIndex = it },
        )

        Spacer(Modifier.height(20.dp))

        AuthTextField(value = name, onValueChange = { name = it }, label = "Name")
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone",
            keyboardType = KeyboardType.Phone
        )

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                PasswordToggle(
                    isPasswordMasked,
                    onToggle = { isPasswordMasked = !isPasswordMasked })
            },
            visualTransformation = if (isPasswordMasked) PasswordVisualTransformation() else VisualTransformation.None,
        )
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                PasswordToggle(
                    isConfirmPasswordMasked,
                    onToggle = { isConfirmPasswordMasked = !isConfirmPasswordMasked })
            },
            visualTransformation = if (isConfirmPasswordMasked) PasswordVisualTransformation() else VisualTransformation.None,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                onRegister(
                    name,
                    email,
                    phone,
                    password,
                    confirmPassword,
                    roleOptions[selectedRoleIndex].id
                )
            },
            enabled = !isRegistering,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roleOptions[selectedRoleIndex].color,
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

//add pass,encrypted,doesnt return to the user,and is used in login
@Composable
private fun RoleSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Blush.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(4.dp),
    ) {
        roleOptions.forEachIndexed { index, role ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) role.color else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    role.label,
                    style = AppType.Label,
                    color = if (selected) AppColors.Surface else AppColors.Terracotta,
                )
            }
        }
    }
}