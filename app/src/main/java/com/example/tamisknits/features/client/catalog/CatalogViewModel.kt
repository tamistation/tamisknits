package com.example.tamisknits.features.client.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.common.ClientFavoritesUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.navigation.ClientRoute
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

private data class CatalogControls(
    val query: String = "",
    val filters: CatalogFilters = CatalogFilters(),
    val sort: CatalogSort = CatalogSort.NEWEST,
    val layout: CatalogLayout = CatalogLayout.GRID,
    val showFilters: Boolean = false,
    val focusSearch: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val useCase: CatalogUseCase,
    private val favoritesUseCase: ClientFavoritesUseCase,
    userUseCase: ClientUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val route = savedStateHandle.toRoute<ClientRoute.Catalog>()
    private val retryTrigger = MutableStateFlow(0)

    private val controls = MutableStateFlow(
        CatalogControls(
            filters = CatalogFilters(category = route.category),
            focusSearch = route.focusSearch
        )
    )

    private val favorites = favoritesUseCase.getFavorites(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<CatalogUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getProducts() },
        favorites,
        controls
    ) { products, favs, control ->
        val base = CatalogUiState(
            query = control.query,
            filters = control.filters,
            sort = control.sort,
            layout = control.layout,
            showFilters = control.showFilters,
            focusSearch = control.focusSearch,
            favoriteIds = favs.map { it.productId }.toSet(),
            message = control.message
        )
        when (products) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(isLoading = false, errorMessage = products.message)
            is ClientResult.Success -> base.copy(
                isLoading = false,
                isOffline = products.isFromCache,
                products = useCase.filterAndSort(
                    products.data,
                    control.query,
                    control.filters,
                    control.sort
                ),
                totalCount = products.data.size,
                categories = useCase.categories(products.data),
                colors = useCase.colors(products.data),
                priceBounds = useCase.priceBounds(products.data)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogUiState())

    fun onQueryChange(query: String) {
        controls.update { it.copy(query = query) }
    }

    fun onSearchFocused() {
        controls.update { it.copy(focusSearch = false) }
    }

    fun onCategorySelected(category: String?) {
        controls.update { it.copy(filters = it.filters.copy(category = category)) }
    }

    fun onColorToggled(color: String) {
        controls.update { control ->
            val colors = control.filters.colors
            val updated = if (colors.any { it.equals(color, ignoreCase = true) }) {
                colors.filterNot { it.equals(color, ignoreCase = true) }.toSet()
            } else {
                colors + color
            }
            control.copy(filters = control.filters.copy(colors = updated))
        }
    }

    // A range covering the full bounds is the same as no price filter, so it isn't counted as active.
    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) {
        val bounds = uiState.value.priceBounds
        val isFullRange = range.start <= bounds.start && range.endInclusive >= bounds.endInclusive
        controls.update {
            it.copy(filters = it.filters.copy(priceRange = if (isFullRange) null else range))
        }
    }

    fun onStockFilterChange(stock: StockFilter) {
        controls.update { it.copy(filters = it.filters.copy(stock = stock)) }
    }

    fun onSortChange(sort: CatalogSort) {
        controls.update { it.copy(sort = sort) }
    }

    fun toggleLayout() {
        controls.update {
            it.copy(layout = if (it.layout == CatalogLayout.GRID) CatalogLayout.LIST else CatalogLayout.GRID)
        }
    }

    fun showFilters(show: Boolean) {
        controls.update { it.copy(showFilters = show) }
    }

    fun clearFilters() {
        controls.update { it.copy(filters = CatalogFilters(), query = "") }
    }

    fun retry() {
        retryTrigger.value++
    }

    fun toggleFavorite(productId: String) {
        favoritesUseCase.toggleFavorite(uid, productId, favorites.value) { error ->
            controls.update { it.copy(message = error) }
        }
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }
}
