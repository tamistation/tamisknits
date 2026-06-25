package com.example.tamisknits

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.example.tamisknits.models.User
import com.example.tamisknits.models.Product
import com.example.tamisknits.models.Order
import com.example.tamisknits.models.SupportTicket
import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.models.Cart
import com.example.tamisknits.models.FavoriteItem

object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()


    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(user.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user") }
    }

    // GET single user
    fun getUser(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = User(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        role = doc.getString("role") ?: "client",

                        )
                    onSuccess(user)
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user") }
    }

    // GET all users
    fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    User(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        role = doc.getString("role") ?: "client"
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get users") }
    }

    // EDIT user
    fun updateUser(uid: String, updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user") }
    }

    // DELETE user
    fun deleteUser(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user") }
    }

    // ════════════════════════════════════════
    // PRODUCTS
    // ════════════════════════════════════════

    // ADD product
    fun addProduct(product: Product, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "imageUrl" to product.imageUrl,
            "stock" to product.stock,
            "isCustomizable" to product.isCustomizable,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("products").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add product") }
    }

    // GET all products
    fun getAllProducts(onSuccess: (List<Product>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Product(
                        productId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        isCustomizable = doc.getBoolean("isCustomizable") ?: false
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get products") }
    }

    // GET single product
    fun getProduct(productId: String, onSuccess: (Product) -> Unit, onFailure: (String) -> Unit) {
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(Product(
                        productId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        isCustomizable = doc.getBoolean("isCustomizable") ?: false
                    ))
                } else {
                    onFailure("Product not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get product") }
    }

    // EDIT product
    fun updateProduct(productId: String, updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("products").document(productId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update product") }
    }

    // DELETE product
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete product") }
    }

    // ════════════════════════════════════════
    // ORDERS
    // ════════════════════════════════════════

    // ADD order
    fun addOrder(order: Order, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to order.clientId,
            "deliveryId" to order.deliveryId,
            "items" to order.items,
            "totalPrice" to order.totalPrice,
            "status" to order.status,
            "shippingAddress" to order.shippingAddress,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("orders").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add order") }
    }

    // GET all orders
    fun getAllOrders(onSuccess: (List<Order>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("orders")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Order(
                        orderId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        deliveryId = doc.getString("deliveryId") ?: "",
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        status = doc.getString("status") ?: "pending",
                        isCustomOrder = doc.getBoolean("isCustomOrder") ?: false
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get orders") }
    }

    // GET orders by client
    fun getOrdersByClient(clientId: String, onSuccess: (List<Order>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("orders")
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Order(
                        orderId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                        status = doc.getString("status") ?: "pending",
                        isCustomOrder = doc.getBoolean("isCustomOrder") ?: false
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get orders") }
    }

    // EDIT order status
    fun updateOrderStatus(orderId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("orders").document(orderId)
            .update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update order") }
    }

    // ════════════════════════════════════════
    // SUPPORT TICKETS
    // ════════════════════════════════════════

    // ADD ticket
    fun addSupportTicket(ticket: SupportTicket, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to ticket.clientId,
            "subject" to ticket.subject,
            "message" to ticket.message,
            "status" to "open",
            "adminReply" to "",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("support_tickets").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add ticket") }
    }

    // GET all tickets
    fun getAllTickets(onSuccess: (List<SupportTicket>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("support_tickets")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    SupportTicket(
                        ticketId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        subject = doc.getString("subject") ?: "",
                        message = doc.getString("message") ?: "",
                        status = doc.getString("status") ?: "open",
                        adminReply = doc.getString("adminReply") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get tickets") }
    }

    // REPLY to ticket
    fun replyToTicket(ticketId: String, reply: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("support_tickets").document(ticketId)
            .update(mapOf("adminReply" to reply, "status" to "resolved", "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to reply") }
    }

    // ════════════════════════════════════════
    // CUSTOMIZATION REQUESTS
    // ════════════════════════════════════════

    // ADD request
    fun addCustomizationRequest(req: CustomizationRequest, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to req.clientId,
            "productId" to req.productId,
            "details" to req.details,
            "colorPreference" to req.colorPreference,
            "sizePreference" to req.sizePreference,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("customization_requests").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add request") }
    }

    // GET all requests
    fun getAllCustomizationRequests(onSuccess: (List<CustomizationRequest>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("customization_requests")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    CustomizationRequest(
                        requestId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        details = doc.getString("details") ?: "",
                        colorPreference = doc.getString("colorPreference") ?: "",
                        sizePreference = doc.getString("sizePreference") ?: "",
                        status = doc.getString("status") ?: "pending"
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get requests") }
    }

    // EDIT request status
    fun updateCustomizationStatus(requestId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("customization_requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update request") }
    }

    // ════════════════════════════════════════
    // CART
    // ════════════════════════════════════════

    // ADD to cart
    fun addToCart(item: Cart, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to item.clientId,
            "productId" to item.productId,
            "quantity" to item.quantity,
            "totalPrice" to item.totalPrice,
            "addedAt" to FieldValue.serverTimestamp()
        )
        db.collection("cart").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to cart") }
    }

    // GET cart items by client
    fun getCartByClient(clientId: String, onSuccess: (List<Cart>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("cart")
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Cart(
                        cartId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get cart") }
    }

    // DELETE from cart
    fun removeFromCart(cartId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("cart").document(cartId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from cart") }
    }

    // ════════════════════════════════════════
    // FAVORITES
    // ════════════════════════════════════════

    // ADD to favorites
    fun addToFavorites(item: FavoriteItem, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to item.clientId,
            "productId" to item.productId,
            "addedAt" to FieldValue.serverTimestamp()
        )
        db.collection("favorites").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to favorites") }
    }

    // GET favorites by client
    fun getFavoritesByClient(clientId: String, onSuccess: (List<FavoriteItem>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("favorites")
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    FavoriteItem(
                        favoriteId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get favorites") }
    }

    // DELETE from favorites
    fun removeFromFavorites(favoriteId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("favorites").document(favoriteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from favorites") }
    }
}
