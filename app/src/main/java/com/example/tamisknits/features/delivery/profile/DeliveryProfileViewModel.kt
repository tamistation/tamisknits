package com.example.tamisknits.features.delivery.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.dataOrNull
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryProfileViewModel @Inject constructor(
    private val useCase: DeliveryProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryProfileUiState(email = useCase.currentEmail))
    val uiState: StateFlow<DeliveryProfileUiState> = _uiState

    private var ordersListener: ListenerRegistration? = null
    private var profileJob: Job? = null

    init {
        load()
    }

    private fun load() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            useCase.getProfile().collect { result ->
                _uiState.update {
                    when (result) {
                        ClientResult.Loading -> it.copy(isLoading = true, errorMessage = null)
                        is ClientResult.Error -> it.copy(isLoading = false, errorMessage = result.message)
                        is ClientResult.Success -> it.copy(
                            isLoading = false,
                            errorMessage = null,
                            user = result.dataOrNull,
                            email = result.dataOrNull?.email?.ifBlank { null } ?: useCase.currentEmail
                        )
                    }
                }
            }
        }

        ordersListener?.remove()
        ordersListener = useCase.listenToMyOrders(
            onUpdate = { orders ->
                _uiState.update { it.copy(deliveredThisWeek = useCase.deliveredThisWeek(orders)) }
            },
            // The stat is secondary; keep the last value rather than failing the whole screen.
            onFailure = { }
        )
    }

    fun retry() = load()

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    // Same approach as the client ProfileViewModel.logout.
    fun logout(onLoggedOut: () -> Unit) {
        _uiState.update { it.copy(showLogoutDialog = false) }
        ordersListener?.remove()
        ordersListener = null
        useCase.signOut()
        onLoggedOut()
    }

    override fun onCleared() {
        ordersListener?.remove()
        ordersListener = null
        super.onCleared()
    }
}
