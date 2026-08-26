package com.example.tamisknits.features.admin.ordersummary

import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.Products
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class OrderSummaryUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {

    fun getOrderById(
        orderId: String,
        onSuccess: (Orders) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.getOrderById(orderId, onSuccess, onFailure)
    }

    private suspend fun getProductSuspend(
        productId: String
    ) = suspendCancellableCoroutine<Products?> { cont ->
        repository.getProduct(
            productId = productId,
            onSuccess = { product ->
                cont.resume(product) {}
            },
            onFailure = {
                cont.resume(null) {}
            }
        )
    }

    suspend fun getOrderItemsWithProductInfo(
        items: List<Map<String, Any>>
    ): List<OrderItem> = coroutineScope {

        items.map { item ->
            async {
                val productId = item["productId"] as? String ?: ""
                val quantity = (item["quantity"] as? Long)?.toInt() ?: 0

                val product = getProductSuspend(productId)
                val firstVariant = product?.variants?.firstOrNull()

                OrderItem(
                    productId = productId,
                    quantity = quantity,
                    name = product?.name ?: "Unknown product",
                    imageUrl = product?.imageUrl ?: "",
                    size = firstVariant?.size ?: "",
                    color = firstVariant?.color ?: "",
                    price = firstVariant?.price ?: 0.0
                )
            }
        }.awaitAll()
    }
}
