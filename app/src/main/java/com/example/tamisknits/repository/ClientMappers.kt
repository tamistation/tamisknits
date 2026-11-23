package com.example.tamisknits.repository

import com.example.tamisknits.models.Cart
import com.example.tamisknits.models.CartLine
import com.example.tamisknits.models.ClientCustomizationRequest
import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientTicket
import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.ProductReview
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.models.SupportTickets
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.millis(field: String): Long =
    (get(field) as? Timestamp)?.toDate()?.time ?: 0L

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.mapList(field: String): List<Map<String, Any>> =
    (get(field) as? List<*>)?.mapNotNull { it as? Map<String, Any> } ?: emptyList()

private fun DocumentSnapshot.stringList(field: String): List<String> =
    (get(field) as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.stringMap(field: String): Map<String, String> =
    (get(field) as? Map<String, Any?>)
        ?.mapValues { (_, value) -> value?.toString().orEmpty() }
        ?: emptyMap()

internal fun DocumentSnapshot.toClientProduct(): ClientProduct = ClientProduct(
    product = Products(
        productId = id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        category = getString("category") ?: "",
        isCustomizable = getBoolean("isCustomizable") ?: false,
        imageUrl = getString("imageUrl") ?: "",
        variants = mapList("variants").map { v ->
            ProductVariant(
                variantId = v["variantId"] as? String ?: "",
                size = v["size"] as? String ?: "",
                color = v["color"] as? String ?: "",
                stock = (v["stock"] as? Number)?.toInt() ?: 0,
                price = (v["price"] as? Number)?.toDouble() ?: 0.0,
                imageUrl = v["imageUrl"] as? String ?: ""
            )
        }
    ),
    materials = getString("materials") ?: "",
    dimensions = getString("dimensions") ?: "",
    images = stringList("images"),
    isFeatured = getBoolean("isFeatured") ?: false,
    soldCount = (get("soldCount") as? Number)?.toInt() ?: 0,
    createdAt = millis("createdAt")
)

internal fun DocumentSnapshot.toCartLine(): CartLine = CartLine(
    item = Cart(
        cartId = id,
        clientId = getString("clientId") ?: "",
        productId = getString("productId") ?: "",
        quantity = (get("quantity") as? Number)?.toInt() ?: 1,
        totalPrice = (get("totalPrice") as? Number)?.toDouble() ?: 0.0,
        deliveryId = getString("deliveryId") ?: ""
    ),
    variantId = getString("variantId") ?: "",
    addedAt = millis("addedAt")
)

internal fun DocumentSnapshot.toClientOrder(): ClientOrder {
    val rawItems = mapList("items")
    return ClientOrder(
        order = Orders(
            orderId = id,
            clientId = getString("clientId") ?: "",
            deliveryId = getString("deliveryId") ?: "",
            items = rawItems,
            totalPrice = (get("totalPrice") as? Number)?.toDouble() ?: 0.0,
            status = getString("status") ?: "pending",
            shippingAddress = stringMap("shippingAddress"),
            isCustomOrder = getBoolean("isCustomOrder") ?: false
        ),
        items = rawItems.map { it.toOrderItem() },
        paymentMethod = getString("paymentMethod") ?: PaymentMethods.CASH_ON_DELIVERY,
        orderNotes = getString("orderNotes") ?: "",
        cancellationReason = getString("cancellationReason") ?: "",
        createdAt = millis("createdAt"),
        updatedAt = millis("updatedAt")
    )
}

internal fun Map<String, Any>.toOrderItem(): OrderItem = OrderItem(
    productId = this["productId"] as? String ?: "",
    variantId = this["variantId"] as? String ?: "",
    quantity = (this["quantity"] as? Number)?.toInt() ?: 0,
    name = this["name"] as? String ?: "",
    imageUrl = this["imageUrl"] as? String ?: "",
    size = this["size"] as? String ?: "",
    color = this["color"] as? String ?: "",
    price = (this["price"] as? Number)?.toDouble() ?: 0.0
)

internal fun OrderItem.toFirestoreMap(): Map<String, Any> = mapOf(
    "productId" to productId,
    "variantId" to variantId,
    "quantity" to quantity,
    "name" to name,
    "imageUrl" to imageUrl,
    "size" to size,
    "color" to color,
    "price" to price
)

internal fun DocumentSnapshot.toSavedAddress(): SavedAddress = SavedAddress(
    addressId = id,
    label = getString("label") ?: "Home",
    fullName = getString("fullName") ?: "",
    phone = getString("phone") ?: "",
    street = getString("street") ?: "",
    building = getString("building") ?: "",
    city = getString("city") ?: "",
    notes = getString("notes") ?: "",
    isDefault = getBoolean("isDefault") ?: false
)

internal fun SavedAddress.toFirestoreMap(): Map<String, Any> = mapOf(
    "label" to label,
    "fullName" to fullName,
    "phone" to phone,
    "street" to street,
    "building" to building,
    "city" to city,
    "notes" to notes,
    "isDefault" to isDefault
)

internal fun DocumentSnapshot.toClientCustomizationRequest() = ClientCustomizationRequest(
    request = CustomizationRequest(
        requestId = id,
        clientId = getString("clientId") ?: "",
        adminId = getString("adminId") ?: "",
        productId = getString("productId") ?: "",
        details = getString("details") ?: "",
        colorPreference = getString("colorPreference") ?: "",
        sizePreference = getString("sizePreference") ?: "",
        status = getString("status") ?: "pending"
    ),
    referenceImageUrl = getString("referenceImageUrl") ?: "",
    createdAt = millis("createdAt")
)

internal fun DocumentSnapshot.toProductReview() = ProductReview(
    reviewId = id,
    productId = getString("productId") ?: "",
    clientId = getString("clientId") ?: "",
    clientName = getString("clientName") ?: "",
    rating = (get("rating") as? Number)?.toInt() ?: 0,
    comment = getString("comment") ?: "",
    createdAt = millis("createdAt"),
    updatedAt = millis("updatedAt")
)

internal fun DocumentSnapshot.toSupportTicket() = SupportTickets(
    ticketId = id,
    clientId = getString("clientId") ?: "",
    adminId = getString("adminId") ?: "",
    subject = getString("subject") ?: "",
    message = getString("message") ?: "",
    status = getString("status") ?: "open"
)

internal fun DocumentSnapshot.toClientTicket() = ClientTicket(
    ticket = toSupportTicket(),
    createdAt = millis("createdAt")
)
