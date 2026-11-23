package com.example.tamisknits.models

// An order as the assigned driver sees it: the client order plus the fields only drivers write.
data class DeliveryOrder(
    val order: ClientOrder = ClientOrder(),
    val deliveredAt: Long = 0L,
    val cashCollected: Boolean = false,
    val deliveryIssue: String = ""
) {
    val orderId: String get() = order.orderId
    val status: String get() = order.status
}
