package com.example.tamisknits.models

import java.util.concurrent.TimeUnit

data class DeliveryEstimate(
    val earliest: Long = 0L,
    val latest: Long = 0L
) {
    companion object {
        private const val MIN_DAYS = 2L
        private const val MAX_DAYS = 4L

        // No delivery date is stored on orders, so the window is counted from when the order was placed.
        fun from(order: ClientOrder): DeliveryEstimate {
            val start = order.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis()
            val day = TimeUnit.DAYS.toMillis(1)
            return DeliveryEstimate(
                earliest = start + MIN_DAYS * day,
                latest = start + MAX_DAYS * day
            )
        }
    }
}
