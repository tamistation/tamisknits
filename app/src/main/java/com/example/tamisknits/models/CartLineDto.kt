package com.example.tamisknits.models

// Wraps the existing Cart document; variantId and addedAt are extra fields the client writes
// on cart documents, kept here so the Cart model itself stays unchanged.
data class CartLine(
    val item: Cart = Cart(),
    val variantId: String = "",
    val addedAt: Long = 0L,
    val product: ClientProduct? = null
) {
    // Older cart documents were written without a variantId, so fall back to the default variant.
    val variant: ProductVariant?
        get() = product?.findVariant(variantId) ?: product?.defaultVariant()

    val name: String get() = product?.name ?: "Unavailable item"
    val unitPrice: Double get() = variant?.price ?: 0.0
    val lineTotal: Double get() = unitPrice * item.quantity
    val availableStock: Int get() = variant?.stock ?: 0
    val isAvailable: Boolean get() = product != null && availableStock > 0
    val exceedsStock: Boolean get() = item.quantity > availableStock

    fun toOrderItem(): OrderItem = OrderItem(
        productId = item.productId,
        variantId = variant?.variantId ?: variantId,
        quantity = item.quantity,
        name = name,
        imageUrl = variant?.imageUrl?.ifBlank { null } ?: product?.product?.imageUrl.orEmpty(),
        size = variant?.size.orEmpty(),
        color = variant?.color.orEmpty(),
        price = unitPrice
    )
}
