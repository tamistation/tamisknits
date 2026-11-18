package com.example.tamisknits.features.client.common

import com.example.tamisknits.features.client.data.repository.ClientCartRepository
import com.example.tamisknits.features.client.data.repository.ClientProductRepository
import com.example.tamisknits.features.pricing.PricingUseCase
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.OrderPricingDto
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.dataOrNull
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

class ClientCartUseCase @Inject constructor(
    private val cartRepository: ClientCartRepository,
    private val productRepository: ClientProductRepository,
    private val repository: FirebaseRepository,
    private val pricingUseCase: PricingUseCase
) {
    fun getCartLines(clientId: String): Flow<ClientResult<List<CartLine>>> = combine(
        cartRepository.getCart(clientId),
        productRepository.getProducts()
    ) { cart, products ->
        combineResults(cart, products) { items, catalog ->
            val byId = catalog.associateBy { it.productId }
            items.map { line -> line.copy(product = byId[line.item.productId]) }
        }
    }

    fun getCartCount(clientId: String): Flow<Int> =
        cartRepository.getCart(clientId)
            .mapNotNull { result -> result.dataOrNull?.sumOf { it.item.quantity } }
            .distinctUntilChanged()

    fun pricingFor(lines: List<CartLine>): OrderPricingDto =
        pricingUseCase.calculatePricing(lines.filter { it.isAvailable }.map { it.toOrderItem() })

    fun addToCart(
        clientId: String,
        product: ClientProduct,
        variant: ProductVariant,
        quantity: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        cartRepository.addToCart(
            clientId = clientId,
            productId = product.productId,
            variantId = variant.variantId,
            quantity = quantity,
            unitPrice = variant.price,
            maxStock = variant.stock,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateQuantity(
        line: CartLine,
        quantity: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.updateCartItem(
            cartId = line.item.cartId,
            quantity = quantity,
            totalPrice = line.unitPrice * quantity,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun remove(cartId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        repository.removeFromCart(cartId, onSuccess, onFailure)
}
