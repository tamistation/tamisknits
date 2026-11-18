package com.example.tamisknits.features.client.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.common.ClientCartUseCase
import com.example.tamisknits.features.client.common.ClientFavoritesUseCase
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.ProductReview
import com.example.tamisknits.models.RatingSummary
import com.example.tamisknits.models.dataOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class DetailsControls(
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val quantity: Int = 1,
    val addToCartStatus: AddToCartStatus = AddToCartStatus.IDLE,
    val showReviewEditor: Boolean = false,
    val reviewRating: Int = 0,
    val reviewComment: String = "",
    val isSavingReview: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val useCase: ProductDetailsUseCase,
    private val cartUseCase: ClientCartUseCase,
    private val favoritesUseCase: ClientFavoritesUseCase,
    userUseCase: ClientUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId = savedStateHandle.toRoute<ClientRoute.ProductDetails>().productId
    private val uid = userUseCase.currentUid
    private val retryTrigger = MutableStateFlow(0)
    private val controls = MutableStateFlow(DetailsControls())
    private var addedResetJob: Job? = null

    private val favorites = favoritesUseCase.getFavorites(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val userName = userUseCase.getProfile()
        .map { it.dataOrNull?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val uiState: StateFlow<ProductDetailsUiState> = combine(
        retryTrigger.flatMapLatest { useCase.getProduct(productId) },
        useCase.getProducts(),
        favorites,
        useCase.getReviews(productId),
        controls
    ) { productResult, allProducts, favs, reviewsResult, control ->
        val favoriteIds = favs.map { it.productId }.toSet()
        val reviews = reviewsResult.dataOrNull.orEmpty()
        val base = ProductDetailsUiState(
            addToCartStatus = control.addToCartStatus,
            showReviewEditor = control.showReviewEditor,
            reviewRating = control.reviewRating,
            reviewComment = control.reviewComment,
            isSavingReview = control.isSavingReview,
            message = control.message,
            isFavorite = productId in favoriteIds,
            relatedFavoriteIds = favoriteIds,
            reviews = reviews,
            ratingSummary = RatingSummary.from(reviews),
            myReview = reviews.find { it.clientId == uid }
        )

        when (productResult) {
            ClientResult.Loading -> base
            is ClientResult.Error -> base.copy(
                isLoading = false,
                errorMessage = productResult.message
            )

            is ClientResult.Success -> {
                val product = productResult.data
                if (product == null) {
                    base.copy(isLoading = false, notFound = true)
                } else {
                    // Until the client picks something, preselect the first in-stock variant.
                    val fallback = product.defaultVariant()
                    val color = control.selectedColor ?: fallback?.color?.takeIf { it.isNotBlank() }
                    val size = control.selectedSize
                        ?: product.variantFor(color, null)?.size?.takeIf { it.isNotBlank() }
                    val stock = product.variantFor(color, size)?.stock ?: 0

                    base.copy(
                        isLoading = false,
                        isOffline = productResult.isFromCache,
                        product = product,
                        selectedColor = color,
                        selectedSize = size,
                        quantity = control.quantity.coerceIn(1, stock.coerceAtLeast(1)),
                        related = allProducts.dataOrNull
                            ?.let { useCase.relatedProducts(product, it) }
                            .orEmpty()
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductDetailsUiState())

    init {
        viewModelScope.launch { useCase.recordView(uid, productId) }
    }

    fun retry() {
        retryTrigger.value++
    }

    fun selectColor(color: String) {
        controls.update { it.copy(selectedColor = color, selectedSize = null, quantity = 1) }
    }

    fun selectSize(size: String) {
        controls.update { it.copy(selectedSize = size, quantity = 1) }
    }

    fun increaseQuantity() {
        val stock = uiState.value.availableStock
        controls.update {
            it.copy(
                quantity = (uiState.value.quantity + 1).coerceAtMost(
                    stock.coerceAtLeast(
                        1
                    )
                )
            )
        }
    }

    fun decreaseQuantity() {
        controls.update { it.copy(quantity = (uiState.value.quantity - 1).coerceAtLeast(1)) }
    }

    fun toggleFavorite() {
        favoritesUseCase.toggleFavorite(uid, productId, favorites.value) { error ->
            controls.update { it.copy(message = error) }
        }
    }

    fun toggleRelatedFavorite(relatedId: String) {
        favoritesUseCase.toggleFavorite(uid, relatedId, favorites.value) { error ->
            controls.update { it.copy(message = error) }
        }
    }

    // The cart listener reflects local writes instantly, so feedback is shown straight away
    // and only rolled back if the write fails.
    fun addToCart() {
        val state = uiState.value
        val product = state.product ?: return
        val variant = state.selectedVariant ?: return
        if (variant.stock <= 0) {
            controls.update { it.copy(message = "This option is out of stock") }
            return
        }

        controls.update { it.copy(addToCartStatus = AddToCartStatus.ADDING) }
        cartUseCase.addToCart(
            clientId = uid,
            product = product,
            variant = variant,
            quantity = state.quantity,
            onSuccess = {},
            onFailure = { error ->
                addedResetJob?.cancel()
                controls.update { it.copy(addToCartStatus = AddToCartStatus.IDLE, message = error) }
            }
        )

        controls.update { it.copy(addToCartStatus = AddToCartStatus.ADDED) }
        addedResetJob?.cancel()
        addedResetJob = viewModelScope.launch {
            delay(ADDED_FEEDBACK_MILLIS)
            controls.update {
                if (it.addToCartStatus == AddToCartStatus.ADDED) it.copy(addToCartStatus = AddToCartStatus.IDLE)
                else it
            }
        }
    }

    fun openReviewEditor() {
        val existing = uiState.value.myReview
        controls.update {
            it.copy(
                showReviewEditor = true,
                reviewRating = existing?.rating ?: 0,
                reviewComment = existing?.comment.orEmpty()
            )
        }
    }

    fun dismissReviewEditor() {
        controls.update { it.copy(showReviewEditor = false) }
    }

    fun onReviewRatingChange(rating: Int) {
        controls.update { it.copy(reviewRating = rating.coerceIn(1, 5)) }
    }

    fun onReviewCommentChange(comment: String) {
        controls.update { it.copy(reviewComment = comment.take(MAX_REVIEW_LENGTH)) }
    }

    fun saveReview() {
        val state = uiState.value
        if (state.reviewRating !in 1..5) {
            controls.update { it.copy(message = "Tap the stars to choose a rating") }
            return
        }
        controls.update { it.copy(isSavingReview = true) }
        useCase.saveReview(
            ProductReview(
                productId = productId,
                clientId = uid,
                clientName = userName.value.ifBlank { "TamisKnits customer" },
                rating = state.reviewRating,
                comment = state.reviewComment.trim(),
                createdAt = state.myReview?.createdAt ?: 0L
            ),
            onSuccess = {
                controls.update {
                    it.copy(
                        isSavingReview = false,
                        showReviewEditor = false,
                        message = "Thanks for your review!"
                    )
                }
            },
            onFailure = { error ->
                controls.update { it.copy(isSavingReview = false, message = error) }
            }
        )
    }

    fun deleteMyReview() {
        useCase.deleteReview(
            productId = productId,
            clientId = uid,
            onSuccess = {
                controls.update { it.copy(showReviewEditor = false, message = "Review removed") }
            },
            onFailure = { error -> controls.update { it.copy(message = error) } }
        )
    }

    fun consumeMessage() {
        controls.update { it.copy(message = null) }
    }

    private companion object {
        const val ADDED_FEEDBACK_MILLIS = 1800L
        const val MAX_REVIEW_LENGTH = 500
    }
}
