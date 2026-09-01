package com.example.tamisknits.features.pricing

import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.OrderPricing
import javax.inject.Inject

class PricingUseCase @Inject constructor() {

    companion object {
        private const val DELIVERY_FEE = 5.0
    }

    fun calculatePricing(
        items: List<OrderItem>,
        discountPercentage: Double = 0.0
    ): OrderPricing {

        val subtotal = items.sumOf { item ->
            item.price * item.quantity
        }

        val discountAmount =
            subtotal * (discountPercentage / 100.0)

        val total =
            subtotal + DELIVERY_FEE - discountAmount

        return OrderPricing(
            subtotal = subtotal,
            deliveryFee = DELIVERY_FEE,
            discountPercentage = discountPercentage,
            discountAmount = discountAmount,
            total = total
        )
    }
}
