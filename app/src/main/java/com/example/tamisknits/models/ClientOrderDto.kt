package com.example.tamisknits.models


data class ClientOrder(
    val order: Orders = Orders(),
    val items: List<OrderItem> = emptyList(),
    val paymentMethod: String = PaymentMethods.CASH_ON_DELIVERY,
    val orderNotes: String = "",
    val cancellationReason: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val orderId: String get() = order.orderId
    val status: String get() = order.status
    val shortId: String get() = order.orderId.take(8).uppercase()
    val itemCount: Int get() = items.sumOf { it.quantity }
}

object PaymentMethods {
    const val CASH_ON_DELIVERY = "cash_on_delivery"

    fun label(method: String): String = when (method) {
        CASH_ON_DELIVERY -> "Cash on delivery"
        else -> method.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
}
