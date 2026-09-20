package com.example.tamisknits.features.admin.userManagement.edit

import androidx.lifecycle.ViewModel
import com.example.tamisknits.features.admin.userManagement.details.UserDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val useCase: UserDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState: StateFlow<EditUserUiState> = _uiState.asStateFlow()

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
                    name = user.name,
                    email = user.email,
                    phone = user.phone,
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

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun saveUser(uid: String, onSuccess: () -> Unit) {
        val state = _uiState.value

        _uiState.value = state.copy(
            isSaving = true,
            error = null
        )

        val updates = mapOf(
            "name" to state.name,
            "email" to state.email,
            "phone" to state.phone
        )

        useCase.updateUser(
            uid = uid,
            updates = updates,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isSaving = false
                )
                onSuccess()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error
                )
            }
        )
    }
}