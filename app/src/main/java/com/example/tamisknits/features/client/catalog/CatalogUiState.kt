package com.example.tamisknits.features.client.catalog

import com.example.tamisknits.models.ClientProduct

enum class CatalogSort(val label: String) {
    NEWEST("Newest"),
    PRICE_LOW_HIGH("Price: low to high"),
    PRICE_HIGH_LOW("Price: high to low"),
    POPULAR("Most loved")
}

enum class CatalogLayout { GRID, LIST }

enum class StockFilter(val label: String) {
    ALL("All pieces"),
    IN_STOCK("In stock only")
}

data class CatalogFilters(
    val category: String? = null,
    val colors: Set<String> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float>? = null,
    val stock: StockFilter = StockFilter.ALL
) {
    val activeCount: Int
        get() = listOf(
            category != null,
            colors.isNotEmpty(),
            priceRange != null,
            stock != StockFilter.ALL
        ).count { it }
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val query: String = "",
    val filters: CatalogFilters = CatalogFilters(),
    val sort: CatalogSort = CatalogSort.NEWEST,
    val layout: CatalogLayout = CatalogLayout.GRID,
    val products: List<ClientProduct> = emptyList(),
    val totalCount: Int = 0,
    val categories: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val priceBounds: ClosedFloatingPointRange<Float> = 0f..0f,
    val favoriteIds: Set<String> = emptySet(),
    val showFilters: Boolean = false,
    val focusSearch: Boolean = false,
    val message: String? = null
) {
    val hasActiveQuery: Boolean
        get() = query.isNotBlank() || filters.activeCount > 0
}
