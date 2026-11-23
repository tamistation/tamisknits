package com.example.tamisknits.features.delivery.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.UserAvatar
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun DeliveryProfilePage(
    viewModel: DeliveryProfileViewModel,
    onChangePassword: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
            .statusBarsPadding()
    ) {
        when {
            uiState.isLoading && uiState.user == null -> LoadingView()
            uiState.errorMessage != null && uiState.user == null -> ErrorView(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::retry
            )

            else -> ProfileContent(
                uiState = uiState,
                onChangePassword = onChangePassword,
                onLogoutClick = { viewModel.showLogoutDialog(true) }
            )
        }
    }

    if (uiState.showLogoutDialog) {
        ConfirmationDialog(
            title = "Log out?",
            message = "You'll need to sign in again to see your deliveries.",
            confirmLabel = "Log out",
            confirmColor = AppColors.Cancelled,
            onConfirm = { viewModel.logout(onLoggedOut) },
            onDismiss = { viewModel.showLogoutDialog(false) }
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: DeliveryProfileUiState,
    onChangePassword: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .readableWidth()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.ItemSpacing)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            UserAvatar(avatarId = AvatarOptions.INITIALS, initials = uiState.initials, size = 88.dp)
            Spacer(Modifier.height(12.dp))
            Text(uiState.displayName, style = AppTextStyles.Title)
            Text("Delivery partner", style = AppTextStyles.BodySmall)
        }

        AppCard {
            Text("Delivered this week", style = AppTextStyles.Overline)
            Spacer(Modifier.height(4.dp))
            Text("${uiState.deliveredThisWeek}", style = AppTextStyles.PriceLarge)
        }

        AppCard {
            InfoRow(Icons.Rounded.Email, "Email", uiState.email.ifBlank { "—" })
            Spacer(Modifier.height(12.dp))
            InfoRow(Icons.Rounded.Phone, "Phone", uiState.user?.phone?.ifBlank { null } ?: "—")
        }

        AppCard(onClick = onChangePassword) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LockReset, contentDescription = null, tint = AppColors.Terracotta)
                Spacer(Modifier.width(12.dp))
                Text("Change password", style = AppTextStyles.Label, modifier = Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.TextMuted
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        SecondaryButton(
            text = "Log out",
            icon = Icons.AutoMirrored.Rounded.Logout,
            onClick = onLogoutClick,
            contentColor = AppColors.Cancelled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = AppTextStyles.Caption)
            Text(value, style = AppTextStyles.Label)
        }
    }
}
