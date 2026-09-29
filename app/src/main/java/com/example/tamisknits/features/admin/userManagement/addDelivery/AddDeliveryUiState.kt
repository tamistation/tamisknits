package com.example.tamisknits.features.admin.userManagement.addDelivery

data class AddDeliveryUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isCreating: Boolean = false,
    val error: String? = null,
)