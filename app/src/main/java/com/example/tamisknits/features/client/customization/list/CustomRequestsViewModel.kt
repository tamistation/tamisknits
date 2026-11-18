package com.example.tamisknits.features.client.customization.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.customization.CustomizationUseCase
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.dataOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomRequestsViewModel @Inject constructor(
    private val useCase: CustomizationUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<CustomRequestsUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getRequests(uid) },
        useCase.getProducts()
    ) { requests, products ->
        val names = products.dataOrNull.orEmpty().associate { it.productId to it.name }
        when (requests) {
            ClientResult.Loading -> CustomRequestsUiState()
            is ClientResult.Error -> CustomRequestsUiState(
                isLoading = false,
                errorMessage = requests.message
            )

            is ClientResult.Success -> CustomRequestsUiState(
                isLoading = false,
                isOffline = requests.isFromCache,
                requests = requests.data,
                productNames = names
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomRequestsUiState())

    fun retry() {
        retryTrigger.value++
    }
}
