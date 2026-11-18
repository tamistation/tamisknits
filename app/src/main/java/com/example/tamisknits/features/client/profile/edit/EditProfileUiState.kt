package com.example.tamisknits.features.client.profile.edit

import com.example.tamisknits.models.AvatarOptions

enum class EditProfileField { NAME, PHONE }

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarId: String = AvatarOptions.INITIALS,
    val fieldErrors: Map<EditProfileField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val message: String? = null
) {
    val initials: String
        get() = AvatarOptions.initials(name.ifBlank { email.substringBefore('@') })
}
