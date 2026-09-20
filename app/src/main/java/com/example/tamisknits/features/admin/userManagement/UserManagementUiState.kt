package com.example.tamisknits.features.admin.userManagement

import com.example.tamisknits.models.User

enum class UserManagementTab {
    CLIENTS,
    DELIVERY
}

data class UserManagementUiState(
    val clients: List<User> = emptyList(),
    val deliveryUsers: List<User> = emptyList(),
    val selectedTab: UserManagementTab = UserManagementTab.CLIENTS,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedUsers: List<User>
        get() = when (selectedTab) {
            UserManagementTab.CLIENTS -> clients
            UserManagementTab.DELIVERY -> deliveryUsers
        }

    val filteredUsers: List<User>
        get() {
            if (searchQuery.isBlank()) return selectedUsers

            return selectedUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        user.phone.contains(searchQuery, ignoreCase = true)
            }
        }
}