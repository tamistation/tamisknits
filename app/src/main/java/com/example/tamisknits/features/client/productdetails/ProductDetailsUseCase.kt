package com.example.tamisknits.features.client.productdetails

import com.example.tamisknits.features.client.data.preferences.ClientPreferences
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.ProductReview
import com.example.tamisknits.repository.ClientProductRepository
import com.example.tamisknits.repository.ClientReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductDetailsUseCase @Inject constructor(
    private val productRepository: ClientProductRepository,
    private val reviewRepository: ClientReviewRepository,
    private val preferences: ClientPreferences
) {
    fun getProduct(productId: String): Flow<ClientResult<ClientProduct?>> =
        productRepository.getProduct(productId)

    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = productRepository.getProducts()

    fun getReviews(productId: String): Flow<ClientResult<List<ProductReview>>> =
        reviewRepository.getReviews(productId)

    suspend fun recordView(clientId: String, productId: String) {
        if (clientId.isNotBlank()) preferences.addRecentlyViewed(clientId, productId)
    }

    fun saveReview(review: ProductReview, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        reviewRepository.saveReview(review, onSuccess, onFailure)

    fun deleteReview(
        productId: String,
        clientId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = reviewRepository.deleteReview(productId, clientId, onSuccess, onFailure)

    // Same category first; topped up with other in-stock pieces so the row never looks sparse.
    fun relatedProducts(product: ClientProduct, all: List<ClientProduct>): List<ClientProduct> {
        val others = all.filter { it.productId != product.productId }
        val sameCategory = others.filter { it.category.equals(product.category, ignoreCase = true) }
        val filler = others.filter { it !in sameCategory && it.isInStock }
            .sortedByDescending { it.createdAt }
        return (sameCategory + filler).take(RELATED_COUNT)
    }

    private companion object {
        const val RELATED_COUNT = 8
    }
}
