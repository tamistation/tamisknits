package com.example.tamisknits.features.client.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.profile.ProfileUseCase
import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.dataOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val useCase: ProfileUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid

    private val _uiState = MutableStateFlow(EditProfileUiState(email = userUseCase.currentEmail))
    val uiState: StateFlow<EditProfileUiState> = _uiState

    // Loaded once rather than observed, so a background profile update never overwrites what's being typed.
    init {
        viewModelScope.launch {
            val user = userUseCase.getProfile().first { it !is ClientResult.Loading }.dataOrNull
            val avatarId = useCase.getAvatarId(uid).first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = user?.name.orEmpty(),
                    phone = user?.phone.orEmpty(),
                    email = user?.email?.ifBlank { null } ?: it.email,
                    avatarId = avatarId.ifBlank { AvatarOptions.INITIALS }
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name.take(MAX_NAME_LENGTH),
                fieldErrors = it.fieldErrors - EditProfileField.NAME
            )
        }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(
                phone = phone,
                fieldErrors = it.fieldErrors - EditProfileField.PHONE
            )
        }
    }

    fun onAvatarSelected(avatarId: String) {
        _uiState.update { it.copy(avatarId = avatarId) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = buildMap {
            if (state.name.isBlank()) put(EditProfileField.NAME, "Please enter your name")
            if (!useCase.isValidPhone(state.phone)) put(
                EditProfileField.PHONE,
                "Please enter a valid phone number"
            )
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch { useCase.setAvatarId(uid, state.avatarId) }
        useCase.updateProfile(
            clientId = uid,
            name = state.name.trim(),
            phone = state.phone.trim(),
            onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
            onFailure = { error -> _uiState.update { it.copy(isSaving = false, message = error) } }
        )
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 60
    }
}
