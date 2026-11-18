package com.example.tamisknits.features.client.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientCartUseCase
import com.example.tamisknits.features.client.common.ClientFavoritesUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.data.repository.ClientProductRepository
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.Favorites
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

private data class FavoritesControls(
    val movingToCartIds: Set<String> = emptySet(),
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCase: ClientFavoritesUseCase,
    private val cartUseCase: ClientCartUseCase,
    private val productRepository: ClientProductRepository,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val controls = MutableStateFlow(FavoritesControls())

    // null until the first snapshot arrives, so "no favorites yet" isn't shown while loading.
    private val favorites: StateFlow<List<Favorites>?> = favoritesUseCase.getFavorites(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<FavoritesUiState> = combine(
        retryTrigger.flatMapLatest { productRepository.getProducts() },
        favorites,
        controls
    ) { products, favs, control ->
        val base =
            FavoritesUiState(movingToCartIds = control.movingToCartIds, message = control.message)
        when {
            products is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = products.message
            )

            products !is ClientResult.Success || favs == null -> base
            else -> {
                val byId = products.data.associateBy { it.productId }
                base.copy(
                    isLoading = false,
                    isOffline = products.isFromCache,
                    products = favs.map { it.productId }.distinct().mapNotNull { byId[it] }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun retry() {
        retryTrigger.value++
    }

    fun removeFavorite(productId: String) {
        favoritesUseCase.toggleFavorite(uid, productId, favorites.value.orEmpty()) { error ->
            controls.update { it.copy(message = error) }
        }
    }

    fun moveToCart(product: ClientProduct) {
        val variant = product.variants.firstOrNull { it.stock > 0 }
        if (variant == null) {
            controls.update { it.copy(message = "${product.name} is out of stock right now") }
            return
        }
        controls.update { it.copy(movingToCartIds = it.movingToCartIds + product.productId) }
        cartUseCase.addToCart(
            clientId = uid,
            product = product,
            variant = variant,
            quantity = 1,
            onSuccess = {
                controls.update {
                    it.copy(
                        movingToCartIds = it.movingToCartIds - product.productId,
                        message = "${product.name} added to your cart"
                    )
                }
            },
            onFailure = { error ->
                controls.update {
                    it.copy(
                        movingToCartIds = it.movingToCartIds - product.productId,
                        message = error
                    )
                }
            }
        )
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }
}
