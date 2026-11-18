package com.example.tamisknits.features.client.profile.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.AvatarOption
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.UserAvatar
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun EditProfilePage(
    viewModel: EditProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            viewModel.consumeSaved()
            onBack()
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "Edit profile", onBack = onBack) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(avatarId = uiState.avatarId, initials = uiState.initials, size = 104.dp)
                Spacer(Modifier.height(16.dp))
                Text("Choose your avatar", style = AppTextStyles.LabelSmall)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AvatarOptions.all) { avatarId ->
                        AvatarOption(
                            avatarId = avatarId,
                            initials = uiState.initials,
                            selected = uiState.avatarId == avatarId,
                            onClick = { viewModel.onAvatarSelected(avatarId) },
                            size = 48.dp
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                AppTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Full name",
                    leadingIcon = Icons.Rounded.Person,
                    error = uiState.fieldErrors[EditProfileField.NAME],
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Phone number",
                    leadingIcon = Icons.Rounded.Phone,
                    error = uiState.fieldErrors[EditProfileField.PHONE],
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = "Email",
                    leadingIcon = Icons.Rounded.Email,
                    enabled = false,
                    supportingText = "Your sign-in email can't be changed here"
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Save changes",
                    onClick = viewModel::save,
                    loading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
