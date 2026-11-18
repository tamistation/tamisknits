package com.example.tamisknits.models

data class ProductReview(
    val reviewId: String = "",
    val productId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class RatingSummary(
    val average: Double = 0.0,
    val count: Int = 0
) {
    companion object {
        fun from(reviews: List<ProductReview>): RatingSummary {
            val rated = reviews.filter { it.rating in 1..5 }
            if (rated.isEmpty()) return RatingSummary()
            return RatingSummary(
                average = rated.sumOf { it.rating }.toDouble() / rated.size,
                count = rated.size
            )
        }
    }
}
