package com.example.tamisknits.features.admin.userManagement.details

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val useCase: UserDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailsUiState())
    val uiState: StateFlow<UserDetailsUiState> = _uiState.asStateFlow()

    fun loadUser(uid: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        useCase.getUser(
            uid = uid,
            onSuccess = { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error
                )
            }
        )
    }

    fun updateUser(
        uid: String,
        updates: Map<String, Any>
    ) {
        _uiState.value = _uiState.value.copy(
            isUpdating = true,
            error = null,
            actionMessage = null
        )

        useCase.updateUser(
            uid = uid,
            updates = updates,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    actionMessage = "User updated successfully"
                )

                loadUser(uid)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = error
                )
            }
        )
    }

    fun toggleBlock(uid: String) {
        val currentUser = _uiState.value.user ?: return

        val newStatus = if (currentUser.status == "blocked") {
            "active"
        } else {
            "blocked"
        }

        updateUser(
            uid = uid,
            updates = mapOf(
                "status" to newStatus
            )
        )
    }

    fun deleteUser(uid: String) {
        _uiState.value = _uiState.value.copy(
            isDeleting = true,
            error = null,
            actionMessage = null
        )

        useCase.deleteUser(
            uid = uid,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    isDeleted = true,
                    actionMessage = "User deleted successfully"
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = error
                )
            }
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            actionMessage = null
        )
    }
}