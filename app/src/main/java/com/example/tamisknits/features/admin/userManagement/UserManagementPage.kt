package com.example.tamisknits.features.admin.userManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tamisknits.models.User
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.AppSearchBar
import com.example.tamisknits.ui.components.PageHeader

@Composable
fun UserManagementPage(
    viewModel: UserManagementViewModel,
    onUserClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUsers()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    UserManagementUI(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onRetry = viewModel::loadUsers,
        onUserClick = onUserClick,
        onBackClick = onBackClick
    )
}


@Composable
private fun UserManagementUI(
    uiState: UserManagementUiState,
    onTabSelected: (UserManagementTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onUserClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {

        PageHeader(
            title = "User Management",
            subtitle = "Manage clients and delivery users",
            onBackClick = onBackClick
        )

        UserManagementTabs(
            selectedTab = uiState.selectedTab,
            clientsCount = uiState.clients.size,
            deliveryCount = uiState.deliveryUsers.size,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search by name, email, or phone"
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }
            }

            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "Something went wrong",
                    onRetry = onRetry
                )
            }

            uiState.filteredUsers.isEmpty() -> {
                EmptyUsersState(
                    tab = uiState.selectedTab,
                    hasSearch = uiState.searchQuery.isNotBlank()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.filteredUsers,
                        key = { it.uid }
                    ) { user ->
                        UserCard(
                            user = user,
                            onClick = { onUserClick(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementTabs(
    selectedTab: UserManagementTab,
    clientsCount: Int,
    deliveryCount: Int,
    onTabSelected: (UserManagementTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Cream)
            .padding(4.dp)
    ) {
        UserTab(
            title = "Clients",
            count = clientsCount,
            selected = selectedTab == UserManagementTab.CLIENTS,
            onClick = { onTabSelected(UserManagementTab.CLIENTS) },
            modifier = Modifier.weight(1f)
        )

        UserTab(
            title = "Delivery",
            count = deliveryCount,
            selected = selectedTab == UserManagementTab.DELIVERY,
            onClick = { onTabSelected(UserManagementTab.DELIVERY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UserTab(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppColors.Terracotta else AppColors.Cream)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (selected) AppColors.Surface else AppColors.TextDark,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.size(6.dp))

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (selected) AppColors.DarkTerracotta else AppColors.Blush)
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                color = if (selected) AppColors.Surface else AppColors.Terracotta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Surface)
            .border(
                width = 1.dp,
                color = AppColors.Outline,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppColors.Blush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = AppColors.Terracotta
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = user.name.ifBlank { "Unnamed User" },
                color = AppColors.TextDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = user.email,
                color = AppColors.TextMuted,
                fontSize = 12.sp
            )

            if (user.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.phone,
                    color = AppColors.TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatusBadge(status = user.status)
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "View user"
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val isBlocked = status.equals("blocked", ignoreCase = true)

    val backgroundColor = if (isBlocked) AppColors.ErrorRedSoft else AppColors.SuccessGreenSoft
    val contentColor = if (isBlocked) AppColors.ErrorRed else AppColors.SuccessGreen

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
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
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyUsersState(
    tab: UserManagementTab,
    hasSearch: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasSearch) {
                    "No users found"
                } else {
                    when (tab) {
                        UserManagementTab.CLIENTS -> "No clients yet"
                        UserManagementTab.DELIVERY -> "No delivery partners yet"
                    }
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasSearch) {
                    "Try searching with a different name or email."
                } else {
                    "Users will appear here when they are added."
                },
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Something went wrong",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = message)

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}