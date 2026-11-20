package com.example.tamisknits.features.client.home

import com.example.tamisknits.features.client.data.preferences.ClientPreferences
import com.example.tamisknits.features.client.data.repository.ClientProductRepository
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.HomeSections
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeUseCase @Inject constructor(
    private val productRepository: ClientProductRepository,
    private val preferences: ClientPreferences
) {
    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = productRepository.getProducts()

    fun getRecentlyViewedIds(clientId: String): Flow<List<String>> =
        preferences.recentlyViewed(clientId)

    fun getHomeCarouselIds(): Flow<List<String>> = productRepository.getHomeCarouselIds()

    // The carousel shows exactly what the admin picked, in their order (deleted products are skipped).
    // With nothing picked it falls back to isFeatured, then the newest in-stock products.
    // Products only get isFeatured/soldCount if someone sets them in Firestore,
    // so every section has a sensible fallback built from data the admin already writes.
    fun buildSections(
        products: List<ClientProduct>,
        recentIds: List<String>,
        carouselIds: List<String> = emptyList()
    ): HomeSections {
        val newest = products.sortedByDescending { it.createdAt }
        val byId = products.associateBy { it.productId }
        val picked = carouselIds.mapNotNull { byId[it] }
        val featured = picked.ifEmpty {
            products.filter { it.isFeatured }
                .ifEmpty { newest.filter { it.isInStock } }
                .take(FEATURED_COUNT)
        }
        val bestSellers = products.filter { it.isInStock }
            .sortedWith(
                compareByDescending<ClientProduct> { it.soldCount }
                    .thenByDescending { it.createdAt }
            )
            .take(SECTION_COUNT)
        val categories = products.map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sorted()

        return HomeSections(
            featured = featured,
            newArrivals = newest.take(SECTION_COUNT),
            bestSellers = bestSellers,
            recentlyViewed = recentIds.mapNotNull { byId[it] },
            categories = categories
        )
    }

    private companion object {
        const val FEATURED_COUNT = 5
        const val SECTION_COUNT = 10
    }
}
