package com.example.tamisknits.features.admin.userManagement.details

import com.example.tamisknits.models.User

data class UserDetailsUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isDeleted: Boolean = false,
)