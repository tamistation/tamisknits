package com.example.tamisknits.features.client.profile.password

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.PasswordToggle
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun ChangePasswordPage(
    viewModel: ChangePasswordViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "Change password", onBack = onBack) }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.success,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(padding),
            label = "passwordState"
        ) { success ->
            if (success) {
                EmptyView(
                    icon = Icons.Rounded.LockReset,
                    title = "Password updated",
                    message = "Use your new password the next time you sign in.",
                    actionLabel = "Done",
                    onAction = {
                        viewModel.consumeSuccess()
                        onBack()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier
                            .readableWidth()
                            .padding(horizontal = AppDimens.ScreenPadding, vertical = 12.dp)
                    ) {
                        Text(
                            "For your security, enter your current password before choosing a new one.",
                            style = AppTextStyles.Body
                        )
                        Spacer(Modifier.height(20.dp))
                        PasswordField(
                            value = uiState.currentPassword,
                            onValueChange = viewModel::onCurrentPasswordChange,
                            label = "Current password",
                            error = uiState.fieldErrors[PasswordField.CURRENT]
                        )
                        Spacer(Modifier.height(8.dp))
                        PasswordField(
                            value = uiState.newPassword,
                            onValueChange = viewModel::onNewPasswordChange,
                            label = "New password",
                            error = uiState.fieldErrors[PasswordField.NEW],
                            supportingText = "At least 8 characters"
                        )
                        Spacer(Modifier.height(8.dp))
                        PasswordField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = "Confirm new password",
                            error = uiState.fieldErrors[PasswordField.CONFIRM],
                            imeAction = ImeAction.Done
                        )
                        Spacer(Modifier.height(24.dp))
                        PrimaryButton(
                            text = "Update password",
                            onClick = viewModel::save,
                            loading = uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    var masked by rememberSaveable { mutableStateOf(true) }
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        error = error,
        supportingText = supportingText,
        leadingIcon = Icons.Rounded.Lock,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
        trailingContent = {
            PasswordToggle(
                isPasswordMasked = masked,
                onToggle = { masked = !masked })
        }
    )
}
