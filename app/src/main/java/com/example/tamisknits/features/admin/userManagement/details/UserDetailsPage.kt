package com.example.tamisknits.features.admin.userManagement.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.User
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.PageHeader

@Composable
fun UserDetailsPage(
    uid: String,
    viewModel: UserDetailsViewModel,
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onEditClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uid) {
        viewModel.loadUser(uid)
    }
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleteSuccess()
        }
    }
    UserDetailsUI(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = {
            viewModel.loadUser(uid)
        },
        onEditClick = onEditClick,
        onBlockClick = {
            viewModel.toggleBlock(uid)
        },
        onDeleteClick = {
            viewModel.deleteUser(uid)
        }

    )
}

@Composable
private fun UserDetailsUI(
    uiState: UserDetailsUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onEditClick: () -> Unit,
    onBlockClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {
        PageHeader(
            title = "User Details",
            subtitle = "View and manage user account",
            onBackClick = onBackClick,
        )

        when {
            uiState.isLoading -> {
                LoadingState()
            }

            uiState.error != null && uiState.user == null -> {
                ErrorState(
                    message = uiState.error ?: "Something went wrong",
                    onRetry = onRetry,
                )
            }

            uiState.user != null -> {
                UserDetailsContent(
                    user = uiState.user,
                    isUpdating = uiState.isUpdating,
                    isDeleting = uiState.isDeleting,
                    onBlockClick = onBlockClick,
                    onDeleteClick = {
                        showDeleteDialog = true
                    },
                    onEditClick = onEditClick
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteUserDialog(
            userName = uiState.user?.name ?: "this user",
            isDeleting = uiState.isDeleting,
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            },
            onDismiss = {
                if (!uiState.isDeleting) {
                    showDeleteDialog = false
                }
            },
        )
    }


}

@Composable
private fun UserDetailsContent(
    user: User,
    isUpdating: Boolean,
    isDeleting: Boolean,
    onBlockClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            UserInfoCard(user = user)
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            ActionButton(
                text = "Edit User",
                icon = Icons.Outlined.Edit,
                enabled = !isUpdating && !isDeleting,
                onClick = onEditClick,

            )
        }

        item {
            ActionButton(
                text = if (user.status.equals("blocked", ignoreCase = true)) {
                    "Unblock User"
                } else {
                    "Block User"
                },
                icon = Icons.Outlined.Block,
                enabled = !isUpdating && !isDeleting,
                onClick = onBlockClick,
            )
        }

        item {
            ActionButton(
                text = "Delete User",
                icon = Icons.Outlined.Delete,
                enabled = !isUpdating && !isDeleting,
                destructive = true,
                onClick = onDeleteClick,
            )
        }
    }
}

@Composable
private fun UserInfoCard(
    user: User,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .border(
                width = 1.dp,
                color = AppColors.Outline,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppColors.Blush)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = AppColors.Terracotta,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = user.name.ifBlank { "Unnamed User" },
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = AppColors.TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.email,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = AppColors.TextMuted,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        UserDetailRow(
            label = "Phone",
            value = user.phone.ifBlank { "Not provided" },
        )

        UserDetailRow(
            label = "Role",
            value = user.userType.type.ifBlank { "Unknown" }
                .replaceFirstChar { it.uppercase() },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Status",
                color = AppColors.TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )

            StatusBadge(status = user.status)
        }
    }
}

@Composable
private fun UserDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = AppColors.TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = value,
            color = AppColors.TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusBadge(
    status: String,
) {
    val isBlocked = status.equals("blocked", ignoreCase = true)

    val backgroundColor =
        if (isBlocked) AppColors.ErrorRedSoft
        else AppColors.SuccessGreenSoft

    val contentColor =
        if (isBlocked) AppColors.ErrorRed
        else AppColors.SuccessGreen

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(contentColor)
        )

        Spacer(modifier = Modifier.size(5.dp))

        Text(
            text = if (isBlocked) "Blocked" else "Active",
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) {
                AppColors.ErrorRedSoft
            } else {
                AppColors.Surface
            },
            contentColor = if (destructive) {
                AppColors.ErrorRed
            } else {
                AppColors.Terracotta
            },
            disabledContainerColor = AppColors.DividerLightTwo,
            disabledContentColor = AppColors.TextMuted,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = AppColors.Terracotta,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = AppColors.TextMuted,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Something went wrong",
                color = AppColors.TextDark,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = AppColors.TextMuted,
                fontSize = 13.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onRetry,
            ) {
                Text(
                    text = "Try again",
                    color = AppColors.Terracotta,
                )
            }
        }
    }
}

@Composable
private fun DeleteUserDialog(
    userName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete User?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $userName? This action cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
            ) {
                Text(
                    text = if (isDeleting) "Deleting..." else "Delete",
                    color = AppColors.ErrorRed,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text("Cancel")
            }
        },
    )
}