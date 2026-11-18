package com.example.tamisknits.features.client.profile.addresses

import com.example.tamisknits.models.SavedAddress

enum class AddressField { FULL_NAME, PHONE, STREET, CITY }

data class AddressesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val addresses: List<SavedAddress> = emptyList(),
    val editor: SavedAddress? = null,
    val editorErrors: Map<AddressField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val confirmDeleteId: String? = null,
    val message: String? = null
) {
    val isEditing: Boolean get() = editor != null
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && addresses.isEmpty()
}
