package com.example.tamisknits.features.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientCartUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ClientAppViewModel @Inject constructor(
    cartUseCase: ClientCartUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    val cartCount: StateFlow<Int> = cartUseCase.getCartCount(userUseCase.currentUid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
