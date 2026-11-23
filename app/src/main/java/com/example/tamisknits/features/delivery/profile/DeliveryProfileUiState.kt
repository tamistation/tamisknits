package com.example.tamisknits.features.delivery.profile

import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.models.User

data class DeliveryProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: User? = null,
    val email: String = "",
    val deliveredThisWeek: Int = 0,
    val showLogoutDialog: Boolean = false
) {
    val displayName: String
        get() = user?.name?.ifBlank { null } ?: email.substringBefore('@')

    val initials: String
        get() = AvatarOptions.initials(displayName)
}
