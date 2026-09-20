package com.example.tamisknits.features.admin.userManagement

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val useCase: UserManagementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        var clientsLoaded = false
        var deliveryLoaded = false

        fun checkFinished() {
            if (clientsLoaded && deliveryLoaded) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }

        useCase.getClients(
            onSuccess = { clients ->
                _uiState.value = _uiState.value.copy(
                    clients = clients
                )

                clientsLoaded = true
                checkFinished()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error
                )
            }
        )

        useCase.getDeliveryUsers(
            onSuccess = { deliveryUsers ->
                _uiState.value = _uiState.value.copy(
                    deliveryUsers = deliveryUsers
                )

                deliveryLoaded = true
                checkFinished()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error
                )
            }
        )
    }

    fun selectTab(tab: UserManagementTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            searchQuery = ""
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }
}