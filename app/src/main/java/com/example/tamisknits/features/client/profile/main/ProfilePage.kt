package com.example.tamisknits.features.client.profile.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.features.authentication.components.BrandWordmark
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.theme.AppType
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.PasswordDialog
import com.example.tamisknits.ui.components.ShimmerBox
import com.example.tamisknits.ui.components.UserAvatar
import com.example.tamisknits.ui.components.readableWidth

private data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ProfilePage(
    viewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onOrders: () -> Unit,
    onAddresses: () -> Unit,
    onCustomRequests: () -> Unit,
    onSupport: () -> Unit,
    onChangePassword: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Cream,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isLoading) {
                    ShimmerBox(Modifier.size(96.dp), CircleShape)
                    Spacer(Modifier.height(14.dp))
                    ShimmerBox(Modifier
                        .width(160.dp)
                        .height(22.dp))
                } else {
                    UserAvatar(
                        avatarId = uiState.avatarId,
                        initials = uiState.initials,
                        size = 96.dp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        uiState.displayName,
                        style = AppType.Brand.copy(fontSize = 30.sp, color = AppColors.TextDark),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        uiState.email,
                        style = AppTextStyles.BodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    AppChip(
                        label = "Edit profile",
                        selected = false,
                        onClick = onEditProfile,
                        leadingIcon = Icons.Rounded.Edit
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "Orders",
                        uiState.totalOrders.toString(),
                        onOrders,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "In progress",
                        uiState.activeOrders.toString(),
                        onOrders,
                        Modifier.weight(1f)
                    )
                }

                MenuSection(
                    title = "Shopping",
                    items = listOf(
                        ProfileMenuItem(
                            Icons.AutoMirrored.Rounded.ReceiptLong,
                            "My orders",
                            "Track and review your orders",
                            onClick = onOrders
                        ),
                        ProfileMenuItem(
                            Icons.Rounded.AutoAwesome,
                            "Custom requests",
                            "Bags made just for you",
                            onClick = onCustomRequests
                        ),
                        ProfileMenuItem(
                            Icons.Rounded.Place,
                            "Saved addresses",
                            "Manage delivery addresses",
                            onClick = onAddresses
                        )
                    )
                )
                MenuSection(
                    title = "Help",
                    items = listOf(
                        ProfileMenuItem(
                            Icons.Rounded.HeadsetMic,
                            "Support & chat",
                            "We're here to help",
                            onClick = onSupport
                        )
                    )
                )
                MenuSection(
                    title = "Account",
                    items = listOf(
                        ProfileMenuItem(
                            Icons.Rounded.Lock,
                            "Change password",
                            onClick = onChangePassword
                        ),
                        ProfileMenuItem(
                            Icons.AutoMirrored.Rounded.Logout,
                            "Log out",
                            destructive = true
                        ) { viewModel.showLogoutDialog(true) },
                        ProfileMenuItem(
                            Icons.Rounded.DeleteForever,
                            "Delete account",
                            "Permanently remove your account",
                            destructive = true
                        ) {
                            viewModel.showDeleteDialog(true)
                        }
                    )
                )
                Spacer(Modifier.height(24.dp))
                BrandWordmark(color = AppColors.Terracotta, size = 32.sp)
            }
        }
    }

    if (uiState.showLogoutDialog) {
        ConfirmationDialog(
            title = "Log out?",
            message = "You'll need to sign in again to see your orders and favourites.",
            confirmLabel = "Log out",
            confirmColor = AppColors.Cancelled,
            onConfirm = { viewModel.logout(onLoggedOut) },
            onDismiss = { viewModel.showLogoutDialog(false) }
        )
    }

    if (uiState.showDeleteDialog) {
        PasswordDialog(
            title = "Delete your account?",
            message = "This removes your profile, cart, favourites, saved addresses and reviews. Your past orders stay with the shop for its records. This can't be undone.",
            confirmLabel = "Delete",
            password = uiState.deletePassword,
            onPasswordChange = viewModel::onDeletePasswordChange,
            error = uiState.deleteError,
            isLoading = uiState.isDeleting,
            onConfirm = { viewModel.deleteAccount(onLoggedOut) },
            onDismiss = { viewModel.showDeleteDialog(false) }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Text(value, style = AppTextStyles.Title.copy(color = AppColors.Terracotta))
        Text(label, style = AppTextStyles.Caption)
    }
}

@Composable
private fun MenuSection(title: String, items: List<ProfileMenuItem>) {
    Column(Modifier
        .fillMaxWidth()
        .padding(top = 24.dp)) {
        Text(
            title.uppercase(),
            style = AppTextStyles.Overline,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        AppCard(contentPadding = PaddingValues(0.dp)) {
            items.forEachIndexed { index, item ->
                MenuRow(item)
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = AppColors.OutlineSoft,
                        modifier = Modifier.padding(start = 68.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuRow(item: ProfileMenuItem) {
    val accent = if (item.destructive) AppColors.Cancelled else AppColors.Terracotta
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (item.destructive) AppColors.CancelButtonBackground else AppColors.Blush)
                .padding(9.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                style = AppTextStyles.Label.copy(color = if (item.destructive) AppColors.Cancelled else AppColors.TextDark)
            )
            if (item.subtitle != null) Text(item.subtitle, style = AppTextStyles.Caption)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AppColors.TextMuted)
    }
}
