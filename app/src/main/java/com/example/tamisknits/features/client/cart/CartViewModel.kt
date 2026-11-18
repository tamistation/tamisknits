package com.example.tamisknits.features.client.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientCartUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.ClientResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class CartControls(
    val updatingCartIds: Set<String> = emptySet(),
    val lastRemoved: CartLine? = null,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartUseCase: ClientCartUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val controls = MutableStateFlow(CartControls())

    val uiState: StateFlow<CartUiState> = combine(
        retryTrigger.flatMapLatest { cartUseCase.getCartLines(uid) },
        controls
    ) { linesResult, control ->
        val base = CartUiState(
            updatingCartIds = control.updatingCartIds,
            lastRemoved = control.lastRemoved,
            message = control.message
        )
        when (linesResult) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = linesResult.message
            )

            is ClientResult.Success -> base.copy(
                isLoading = false,
                isOffline = linesResult.isFromCache,
                lines = linesResult.data,
                pricing = if (linesResult.data.isEmpty()) null else cartUseCase.pricingFor(
                    linesResult.data
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    fun retry() {
        retryTrigger.value++
    }

    fun increase(line: CartLine) {
        if (line.item.quantity >= line.availableStock) {
            controls.update { it.copy(message = "Only ${line.availableStock} left in this colour") }
            return
        }
        setQuantity(line, line.item.quantity + 1)
    }

    fun decrease(line: CartLine) {
        if (line.item.quantity <= 1) return
        setQuantity(line, line.item.quantity - 1)
    }

    fun fixQuantity(line: CartLine) {
        if (line.availableStock <= 0) remove(line) else setQuantity(line, line.availableStock)
    }

    private fun setQuantity(line: CartLine, quantity: Int) {
        val cartId = line.item.cartId
        controls.update { it.copy(updatingCartIds = it.updatingCartIds + cartId) }
        cartUseCase.updateQuantity(
            line = line,
            quantity = quantity,
            onSuccess = { controls.update { it.copy(updatingCartIds = it.updatingCartIds - cartId) } },
            onFailure = { error ->
                controls.update {
                    it.copy(
                        updatingCartIds = it.updatingCartIds - cartId,
                        message = error
                    )
                }
            }
        )
    }

    fun remove(line: CartLine) {
        controls.update { it.copy(lastRemoved = line, message = "${line.name} removed") }
        cartUseCase.remove(
            cartId = line.item.cartId,
            onSuccess = {},
            onFailure = { error ->
                controls.update {
                    it.copy(
                        lastRemoved = null,
                        message = error
                    )
                }
            }
        )
    }

    fun undoRemove() {
        val line = controls.value.lastRemoved ?: return
        val product = line.product ?: return
        val variant = line.variant ?: return
        controls.update { it.copy(lastRemoved = null) }
        cartUseCase.addToCart(
            clientId = uid,
            product = product,
            variant = variant,
            quantity = line.item.quantity,
            onSuccess = {},
            onFailure = { error -> controls.update { it.copy(message = error) } }
        )
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null, lastRemoved = null) }
    }
}
