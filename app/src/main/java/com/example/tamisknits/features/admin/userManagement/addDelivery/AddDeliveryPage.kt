package com.example.tamisknits.features.admin.userManagement.addDelivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.Fraunces
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.PrimaryButton

@Composable
fun AddDeliveryPage(
    viewModel: AddDeliveryViewModel,
    onBackClick: () -> Unit,
    onCreateSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var passwordCopied by remember { mutableStateOf(false) }

    AddDeliveryUI(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::updateName,
        onEmailChange = viewModel::updateEmail,
        onPhoneChange = viewModel::updatePhone,
        onCreateClick = viewModel::createDelivery
    )

    // Shown once: the password isn't stored anywhere, so closing this is the last chance to see it.
    val temporaryPassword = uiState.temporaryPassword
    if (temporaryPassword != null) {
        ConfirmationDialog(
            title = "Driver account created",
            message = "Share these sign-in details with the driver. " +
                    "This password won't be shown again.\n\n" +
                    "Email: ${uiState.createdEmail}\n" +
                    "Temporary password: $temporaryPassword",
            confirmLabel = if (passwordCopied) "Copied" else "Copy password",
            onConfirm = {
                clipboardManager.setText(AnnotatedString(temporaryPassword))
                passwordCopied = true
            },
            onDismiss = onCreateSuccess
        )
    }
}

@Composable
private fun AddDeliveryUI(
    uiState: AddDeliveryUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {

        PageHeader(
            title = "Add Delivery Partner",
            subtitle = "Create a new delivery account",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Account Information",
                fontSize = 18.sp,
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextDark
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Name")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Terracotta,
                    unfocusedBorderColor = AppColors.Outline,
                    focusedContainerColor = AppColors.Surface,
                    unfocusedContainerColor = AppColors.Surface,
                )
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Email")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Terracotta,
                    unfocusedBorderColor = AppColors.Outline,
                    focusedContainerColor = AppColors.Surface,
                    unfocusedContainerColor = AppColors.Surface,
                )
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Phone")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Terracotta,
                    unfocusedBorderColor = AppColors.Outline,
                    focusedContainerColor = AppColors.Surface,
                    unfocusedContainerColor = AppColors.Surface,
                )
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = AppColors.ErrorRed,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Create Delivery Account",
                onClick = onCreateClick,
                loading = uiState.isCreating,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}