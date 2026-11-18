package com.example.tamisknits.models


data class ClientProduct(
    val product: Products = Products(),
    val materials: String = "",
    val dimensions: String = "",
    val images: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val soldCount: Int = 0,
    val createdAt: Long = 0L
) {
    val productId: String get() = product.productId
    val name: String get() = product.name
    val category: String get() = product.category
    val description: String get() = product.description
    val variants: List<ProductVariant> get() = product.variants

    val minPrice: Double get() = variants.minOfOrNull { it.price } ?: 0.0
    val maxPrice: Double get() = variants.maxOfOrNull { it.price } ?: 0.0
    val totalStock: Int get() = variants.sumOf { it.stock }
    val isInStock: Boolean get() = totalStock > 0
    val isLowStock: Boolean get() = totalStock in 1..LOW_STOCK_THRESHOLD

    val colors: List<String>
        get() = variants.map { it.color.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

    val sizes: List<String>
        get() = variants.map { it.size.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

    fun findVariant(variantId: String): ProductVariant? =
        variants.find { it.variantId == variantId }

    fun defaultVariant(): ProductVariant? =
        variants.firstOrNull { it.stock > 0 } ?: variants.firstOrNull()

    fun variantFor(color: String?, size: String?): ProductVariant? {
        val matches = variants.filter { variant ->
            (color == null || variant.color.equals(color, ignoreCase = true)) &&
                    (size == null || variant.size.equals(size, ignoreCase = true))
        }
        return matches.firstOrNull { it.stock > 0 } ?: matches.firstOrNull()
    }

    companion object {
        const val LOW_STOCK_THRESHOLD = 3
    }
}
