package com.example.tamisknits.features.client.profile.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.orders.OrdersUseCase
import com.example.tamisknits.features.client.profile.ProfileUseCase
import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.models.ClientOrderTab
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.dataOrNull
import com.example.tamisknits.models.tabFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class ProfileControls(
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletePassword: String = "",
    val isDeleting: Boolean = false,
    val deleteError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val useCase: ProfileUseCase,
    ordersUseCase: OrdersUseCase,
    private val userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val controls = MutableStateFlow(ProfileControls())

    private val statusConfig = ordersUseCase.getOrderStatusConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStatusConfig())

    val uiState: StateFlow<ProfileUiState> = combine(
        userUseCase.getProfile(),
        useCase.getAvatarId(uid),
        useCase.getOrders(uid),
        statusConfig,
        controls
    ) { profile, avatarId, orders, config, control ->
        val orderList = orders.dataOrNull.orEmpty()
        ProfileUiState(
            isLoading = profile is ClientResult.Loading,
            errorMessage = (profile as? ClientResult.Error)?.message,
            user = profile.dataOrNull,
            email = profile.dataOrNull?.email?.ifBlank { null } ?: userUseCase.currentEmail,
            avatarId = avatarId.ifBlank { AvatarOptions.INITIALS },
            totalOrders = orderList.size,
            activeOrders = orderList.count { config.tabFor(it.status) == ClientOrderTab.ACTIVE },
            showLogoutDialog = control.showLogoutDialog,
            showDeleteDialog = control.showDeleteDialog,
            deletePassword = control.deletePassword,
            isDeleting = control.isDeleting,
            deleteError = control.deleteError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun showLogoutDialog(show: Boolean) {
        controls.update { it.copy(showLogoutDialog = show) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        controls.update { it.copy(showLogoutDialog = false) }
        useCase.signOut()
        onLoggedOut()
    }

    fun showDeleteDialog(show: Boolean) {
        controls.update {
            it.copy(
                showDeleteDialog = show,
                deletePassword = "",
                deleteError = null
            )
        }
    }

    fun onDeletePasswordChange(password: String) {
        controls.update { it.copy(deletePassword = password, deleteError = null) }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val password = controls.value.deletePassword
        if (password.isBlank()) {
            controls.update { it.copy(deleteError = "Enter your password to confirm") }
            return
        }
        controls.update { it.copy(isDeleting = true, deleteError = null) }
        useCase.deleteAccount(
            currentPassword = password,
            onSuccess = {
                viewModelScope.launch {
                    useCase.clearLocalData(uid)
                    controls.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    onDeleted()
                }
            },
            onFailure = { error ->
                controls.update { it.copy(isDeleting = false, deleteError = error) }
            }
        )
    }
}
