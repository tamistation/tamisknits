package com.example.tamisknits.features.client.profile.main

import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.models.User

data class ProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: User? = null,
    val email: String = "",
    val avatarId: String = AvatarOptions.INITIALS,
    val totalOrders: Int = 0,
    val activeOrders: Int = 0,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletePassword: String = "",
    val isDeleting: Boolean = false,
    val deleteError: String? = null
) {
    val displayName: String
        get() = user?.name?.ifBlank { null } ?: email.substringBefore('@')

    val initials: String
        get() = AvatarOptions.initials(displayName)
}
