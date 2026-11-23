package com.example.tamisknits.features.admin.userManagement.addDelivery

data class AddDeliveryUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isCreating: Boolean = false,
    val error: String? = null,
    // Set once the account exists; the page shows the temporary password a single time.
    val createdEmail: String? = null,
    val temporaryPassword: String? = null,
)
