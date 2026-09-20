package com.example.tamisknits.features.admin.userManagement.edit

import com.example.tamisknits.models.User

data class EditUserUiState(
    val user: User? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)