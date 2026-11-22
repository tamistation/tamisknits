package com.example.tamisknits.features.client.catalog

import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.repository.ClientProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

class CatalogUseCase @Inject constructor(
    private val productRepository: ClientProductRepository
) {
    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = productRepository.getProducts()

    fun filterAndSort(
        products: List<ClientProduct>,
        query: String,
        filters: CatalogFilters,
        sort: CatalogSort
    ): List<ClientProduct> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val selectedColors = filters.colors.map { it.lowercase() }.toSet()

        val filtered = products.filter { product ->
            val searchable = buildString {
                append(product.name).append(' ')
                append(product.category).append(' ')
                append(product.description).append(' ')
                append(product.colors.joinToString(" "))
            }.lowercase()

            val matchesQuery = terms.all { searchable.contains(it) }
            val matchesCategory = filters.category == null ||
                    product.category.equals(filters.category, ignoreCase = true)
            val matchesColor = selectedColors.isEmpty() ||
                    product.colors.any { it.lowercase() in selectedColors }
            val matchesPrice = filters.priceRange == null || (
                    product.minPrice <= filters.priceRange.endInclusive &&
                            product.maxPrice >= filters.priceRange.start
                    )
            val matchesStock = filters.stock == StockFilter.ALL || product.isInStock

            matchesQuery && matchesCategory && matchesColor && matchesPrice && matchesStock
        }

        return when (sort) {
            CatalogSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
            CatalogSort.PRICE_LOW_HIGH -> filtered.sortedBy { it.minPrice }
            CatalogSort.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.minPrice }
            CatalogSort.POPULAR -> filtered.sortedWith(
                compareByDescending<ClientProduct> { it.soldCount }.thenByDescending { it.createdAt }
            )
        }
    }

    fun categories(products: List<ClientProduct>): List<String> =
        products.map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sorted()

    fun colors(products: List<ClientProduct>): List<String> =
        products.flatMap { it.colors }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

    fun priceBounds(products: List<ClientProduct>): ClosedFloatingPointRange<Float> {
        val prices = products.flatMap { product -> product.variants.map { it.price } }
        if (prices.isEmpty()) return 0f..0f
        return floor(prices.min()).toFloat()..ceil(prices.max()).toFloat()
    }
}
