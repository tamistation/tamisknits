package com.example.tamisknits.features.client.home

import com.example.tamisknits.features.client.data.preferences.ClientPreferences
import com.example.tamisknits.features.client.data.repository.ClientProductRepository
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.HomeSections
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class HomeUseCase @Inject constructor(
    private val productRepository: ClientProductRepository,
    private val preferences: ClientPreferences
) {
    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = productRepository.getProducts()

    fun getRecentlyViewedIds(clientId: String): Flow<List<String>> =
        preferences.recentlyViewed(clientId)

    // Products only get isFeatured/soldCount if someone sets them in Firestore,
    // so every section has a sensible fallback built from data the admin already writes.
    fun buildSections(products: List<ClientProduct>, recentIds: List<String>): HomeSections {
        val newest = products.sortedByDescending { it.createdAt }
        val featured = products.filter { it.isFeatured }
            .ifEmpty { newest.filter { it.isInStock } }
            .take(FEATURED_COUNT)
        val bestSellers = products.filter { it.isInStock }
            .sortedWith(
                compareByDescending<ClientProduct> { it.soldCount }
                    .thenByDescending { it.createdAt }
            )
            .take(SECTION_COUNT)
        val byId = products.associateBy { it.productId }
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
