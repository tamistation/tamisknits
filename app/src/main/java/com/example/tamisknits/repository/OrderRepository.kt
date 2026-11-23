package com.example.tamisknits.repository

import com.example.tamisknits.models.Cart
import com.example.tamisknits.models.Delivery
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.Orders
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class OrderRepository(
    private val firestore: FirebaseFirestore
) {


    // add delivery
    fun addDelivery(delivery: Delivery, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "orderId" to delivery.orderId,
            "clientId" to delivery.clientId,
            "deliveryPersonId" to delivery.deliveryPersonId,
            "address" to delivery.address,
            "status" to delivery.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Delivery").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add delivery") }
    }

    // get delivery
    fun getDelivery(
        deliveryId: String,
        onSuccess: (Delivery) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Delivery").document(deliveryId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        Delivery(
                            deliveryId = doc.id,
                            orderId = doc.getString("orderId") ?: "",
                            clientId = doc.getString("clientId") ?: "",
                            deliveryPersonId = doc.getString("deliveryPersonId") ?: "",
                            address = doc.getString("address") ?: "",
                            status = doc.getString("status") ?: "pending"
                        )
                    )
                } else {
                    onFailure("Delivery not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get delivery") }
    }

    // update delivery status
    fun updateDeliveryStatus(
        deliveryId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Delivery").document(deliveryId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update delivery") }
    }

    // delete delivery
    fun deleteDelivery(deliveryId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Delivery").document(deliveryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete delivery") }
    }

    // Assigns a driver by storing their User uid in Orders.deliveryId (no Delivery doc).
    // The driver's app listens on Orders where deliveryId == their uid.
    fun assignDriverToOrder(
        orderId: String,
        driverId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .update(mapOf("deliveryId" to driverId, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to assign driver") }
    }


    // add order
    fun addOrder(order: Orders, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to order.clientId,
            "deliveryId" to order.deliveryId,
            "items" to order.items,
            "totalPrice" to order.totalPrice,
            "status" to order.status,
            "shippingAddress" to order.shippingAddress,
            "isCustomOrder" to order.isCustomOrder,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Orders").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add order") }
    }

    // get orders
    fun getOrders(): Flow<List<Orders>> = callbackFlow {
        val listener = firestore.collection("Orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Orders(
                        orderId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        deliveryId = doc.getString("deliveryId") ?: "",
                        items = (doc.get("items") as? List<Map<String, Any>>) ?: emptyList(),
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        status = doc.getString("status") ?: "pending",
                        shippingAddress = (doc.get("shippingAddress") as? Map<String, String>)
                            ?: emptyMap(),
                        isCustomOrder = doc.getBoolean("isCustomOrder") ?: false
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getOrderById(
        orderId: String,
        onSuccess: (Orders) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val order = doc.toObject(Orders::class.java)?.copy(orderId = doc.id)
                    if (order != null) {
                        onSuccess(order)
                    } else {
                        onFailure("Failed to parse order")
                    }
                } else {
                    onFailure("Order not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get order") }
    }

    // get orders by client
    fun getOrdersByClient(
        clientId: String,
        onSuccess: (List<Orders>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders")
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Orders(
                        orderId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        deliveryId = doc.getString("deliveryId") ?: "",
                        items = (doc.get("items") as? List<Map<String, Any>>) ?: emptyList(),
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        status = doc.getString("status") ?: "pending",
                        shippingAddress = (doc.get("shippingAddress") as? Map<String, String>)
                            ?: emptyMap(),
                        isCustomOrder = doc.getBoolean("isCustomOrder") ?: false
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get orders") }
    }

    // edit order status
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update order") }
    }


    // delete order
    fun deleteOrder(orderId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Orders").document(orderId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete order") }
    }


    fun updateOrder(
        orderId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Orders").document(orderId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to update order") }
    }


    // add to cart
    fun addToCart(cart: Cart, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to cart.clientId,
            "productId" to cart.productId,
            "quantity" to cart.quantity,
            "totalPrice" to cart.totalPrice,
            "deliveryId" to cart.deliveryId,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Cart").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to cart") }
    }

    // get cart by client
    fun getCart(clientId: String): Flow<List<Cart>> = callbackFlow {
        val listener = firestore.collection("Cart")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Cart(
                        cartId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        deliveryId = doc.getString("deliveryId") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // edit cart item quantity
    fun updateCartItem(
        cartId: String,
        quantity: Int,
        totalPrice: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Cart").document(cartId)
            .update(mapOf("quantity" to quantity, "totalPrice" to totalPrice))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update cart") }
    }


    fun setCartDelivery(
        cartId: String,
        deliveryId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("Cart").document(cartId)
            .update("deliveryId", deliveryId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to set cart delivery") }
    }

    // delete from cart
    fun removeFromCart(cartId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Cart").document(cartId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from cart") }
    }

    // get order status configuration (pending/active/previous groupings + full flow)
    fun getOrderStatusConfig(): Flow<OrderStatusConfig> = callbackFlow {
        val listener = firestore.collection("config").document("orderStatuses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(OrderStatusConfig()); return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val config = OrderStatusConfig(
                        pending = (snapshot.get("pending") as? List<String>) ?: listOf("pending"),
                        active = (snapshot.get("active") as? List<String>)
                            ?: listOf("confirmed", "shipped", "in_transit"),
                        previous = (snapshot.get("previous") as? List<String>)
                            ?: listOf("delivered", "cancelled"),
                        flow = (snapshot.get("flow") as? List<String>)
                            ?: listOf("pending", "confirmed", "shipped", "in_transit", "delivered")
                    )
                    trySend(config)
                } else {
                    trySend(OrderStatusConfig())
                }
            }
        awaitClose { listener.remove() }
    }
}
