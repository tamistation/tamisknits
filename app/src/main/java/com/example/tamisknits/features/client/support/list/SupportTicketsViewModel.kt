package com.example.tamisknits.features.client.support.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.support.SupportUseCase
import com.example.tamisknits.models.ClientResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SupportTicketsViewModel @Inject constructor(
    private val useCase: SupportUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<SupportTicketsUiState> = retryTrigger
        .flatMapLatest { useCase.getTickets(uid) }
        .map { result ->
            when (result) {
                ClientResult.Loading -> SupportTicketsUiState()
                is ClientResult.Error -> SupportTicketsUiState(
                    isLoading = false,
                    errorMessage = result.message
                )

                is ClientResult.Success -> {
                    val (closed, open) = result.data.partition { it.isClosed }
                    SupportTicketsUiState(
                        isLoading = false,
                        isOffline = result.isFromCache,
                        openTickets = open,
                        closedTickets = closed
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupportTicketsUiState())

    fun retry() {
        retryTrigger.value++
    }
}
