package com.example.tamisknits.features.client.productdetails

import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ProductReview
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.RatingSummary

enum class AddToCartStatus { IDLE, ADDING, ADDED }

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notFound: Boolean = false,
    val isOffline: Boolean = false,
    val product: ClientProduct? = null,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val quantity: Int = 1,
    val isFavorite: Boolean = false,
    val related: List<ClientProduct> = emptyList(),
    val relatedFavoriteIds: Set<String> = emptySet(),
    val reviews: List<ProductReview> = emptyList(),
    val ratingSummary: RatingSummary = RatingSummary(),
    val myReview: ProductReview? = null,
    val addToCartStatus: AddToCartStatus = AddToCartStatus.IDLE,
    val showReviewEditor: Boolean = false,
    val reviewRating: Int = 0,
    val reviewComment: String = "",
    val isSavingReview: Boolean = false,
    val message: String? = null
) {
    val selectedVariant: ProductVariant?
        get() = product?.variantFor(selectedColor, selectedSize)

    val availableStock: Int
        get() = selectedVariant?.stock ?: 0

    val sizesForSelectedColor: List<String>
        get() = product?.variants
            ?.filter { selectedColor == null || it.color.equals(selectedColor, ignoreCase = true) }
            ?.map { it.size.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinctBy { it.lowercase() }
            ?: emptyList()

    val canAddToCart: Boolean
        get() = selectedVariant != null && availableStock > 0 &&
                addToCartStatus != AddToCartStatus.ADDING

    fun isColorAvailable(color: String): Boolean =
        product?.variants?.any { it.color.equals(color, ignoreCase = true) && it.stock > 0 } == true

    fun isSizeAvailable(size: String): Boolean =
        product?.variants?.any {
            it.size.equals(size, ignoreCase = true) && it.stock > 0 &&
                    (selectedColor == null || it.color.equals(selectedColor, ignoreCase = true))
        } == true
}
