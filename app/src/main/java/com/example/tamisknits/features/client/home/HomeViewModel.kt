package com.example.tamisknits.features.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.features.client.common.ClientFavoritesUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
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
class HomeViewModel @Inject constructor(
    private val useCase: HomeUseCase,
    private val favoritesUseCase: ClientFavoritesUseCase,
    userUseCase: ClientUserUseCase
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val message = MutableStateFlow<String?>(null)

    private val favorites = favoritesUseCase.getFavorites(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A Firestore listener stops for good after an error, so retrying means subscribing again.
    val uiState: StateFlow<HomeUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getProducts() },
        favorites,
        useCase.getRecentlyViewedIds(uid),
        userUseCase.getProfile(),
        message
    ) { products, favs, recentIds, profile, currentMessage ->
        val base = HomeUiState(
            userName = profile.dataOrNull?.name?.trim()?.substringBefore(' ').orEmpty(),
            favoriteIds = favs.map { it.productId }.toSet(),
            message = currentMessage
        )
        when (products) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(isLoading = false, errorMessage = products.message)
            is ClientResult.Success -> {
                val sections = useCase.buildSections(products.data, recentIds)
                base.copy(
                    isLoading = false,
                    isOffline = products.isFromCache,
                    featured = sections.featured,
                    newArrivals = sections.newArrivals,
                    bestSellers = sections.bestSellers,
                    recentlyViewed = sections.recentlyViewed,
                    categories = sections.categories
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun retry() {
        retryTrigger.value++
    }

    fun toggleFavorite(productId: String) {
        favoritesUseCase.toggleFavorite(uid, productId, favorites.value) { error ->
            message.value = error
        }
    }

    fun consumeMessage() {
        message.value = null
    }
}
