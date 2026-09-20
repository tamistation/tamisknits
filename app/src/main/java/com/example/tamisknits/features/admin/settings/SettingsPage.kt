package com.example.tamisknits.features.admin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.PageHeader
import com.google.firebase.auth.FirebaseAuth

private data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun SettingsPage(
    onUserManagementClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val administration = listOf(
        SettingsItem(
            icon = Icons.Outlined.Group,
            title = "User Management",
            subtitle = "Manage clients and delivery users",
            onClick = onUserManagementClick
        )
    )

    val session = listOf(
        SettingsItem(
            icon = Icons.Outlined.Logout,
            title = "Log out",
            subtitle = "Sign out of this account",
            destructive = true,
            onClick = { showLogoutConfirm = true }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {
        PageHeader(
            title = "Settings",
            subtitle = "Manage your account & preferences"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSection(title = "Administration", items = administration)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSection(title = "Session", items = session)
    }

    if (showLogoutConfirm) {
        ConfirmationDialog(
            title = "Log out?",
            message = "You'll need to sign in again to access your account.",
            confirmLabel = "Log out",
            confirmColor = AppColors.ErrorRed,
            onConfirm = {
                showLogoutConfirm = false
                FirebaseAuth.getInstance().signOut()
                onLoggedOut()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

@Composable
private fun SettingsSection(title: String, items: List<SettingsItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Outline, RoundedCornerShape(18.dp))
        ) {
            items.forEach { item -> SettingsRow(item) }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    val accentColor = if (item.destructive) AppColors.ErrorRed else AppColors.Terracotta
    val iconBackground = if (item.destructive) AppColors.ErrorRedSoft else AppColors.Blush

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
            tint = accentColor,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground)
                .padding(10.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = item.title,
                color = if (item.destructive) AppColors.ErrorRed else AppColors.TextDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = item.subtitle,
                color = AppColors.TextMuted,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextMuted
        )
    }
}