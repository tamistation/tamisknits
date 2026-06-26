package com.example.tamisknits.repository

import com.example.tamisknits.models.Cart
import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.models.Favorites
import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.Products
import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class FirebaseRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    //users
    // get current logged in user
    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            trySend(null); return@addSnapshotListener
                        }
                        if (document != null && document.exists()) {
                            val user = User(
                                uid = document.id,
                                name = document.getString("name") ?: "",
                                email = document.getString("email") ?: "",
                                phone = document.getString("phone") ?: "",
                                role = document.getString("role") ?: "client"
                            )
                            trySend(user)
                        } else {
                            trySend(null)
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    // add user
    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(user.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user") }
    }

    // get single user
    fun getUser(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            role = doc.getString("role") ?: "client"
                        )
                    )
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user") }
    }

    // edit  user
    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user") }
    }

    // delete user
    fun deleteUser(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("users").document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user") }
    }

    //usertypes

    // add user type
    fun addUserType(userType: UserType, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "usertypeid" to userType.usertypeiid,
            "uid" to userType.uid,
            "type" to userType.type,
            "permissions" to userType.permissions
        )
        firestore.collection("user_types").document(userType.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user type") }
    }

    // get user type
    fun getUserType(uid: String, onSuccess: (UserType) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("user_types").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        UserType(
                            usertypeiid = doc.getString("usertypeid") ?: "",
                            uid = doc.getString("uid") ?: "",
                            type = doc.getString("type") ?: "",
                            permissions = (doc.get("permissions") as? List<String>) ?: emptyList()
                        )
                    )
                } else {
                    onFailure("User type not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user type") }
    }

    // edit user type permissions
    fun updateUserType(
        uid: String,
        newType: String,
        newPermissions: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("user_types").document(uid)
            .update(mapOf("type" to newType, "permissions" to newPermissions))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user type") }
    }

    // delete user type
    fun deleteUserType(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("user_types").document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user type") }
    }

    //products

    // add product
    fun addProduct(product: Products, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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
        firestore.collection("products").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add product") }
    }

    // get all products
    fun getProducts(): Flow<List<Products>> = callbackFlow {
        val listener = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Products(
                        productId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        isCustomizable = doc.getBoolean("isCustomizable") ?: false
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // get single product
    fun getProduct(productId: String, onSuccess: (Products) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        Products(
                            productId = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            stock = (doc.getLong("stock") ?: 0L).toInt(),
                            isCustomizable = doc.getBoolean("isCustomizable") ?: false
                        )
                    )
                } else {
                    onFailure("Product not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get product") }
    }

    // edit product
    fun updateProduct(
        productId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("products").document(productId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update product") }
    }

    // delete product
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("products").document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete product") }
    }

    //orders
    // ADD order
    fun addOrder(order: Orders, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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
        firestore.collection("orders").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add order") }
    }

    // get orders
    fun getOrders(): Flow<List<Orders>> = callbackFlow {
        val listener = firestore.collection("orders")
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

    // get orders by client
    fun getOrdersByClient(
        clientId: String,
        onSuccess: (List<Orders>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("orders")
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
        firestore.collection("orders").document(orderId)
            .update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update order") }
    }

    // delete order
    fun deleteOrder(orderId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("orders").document(orderId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete order") }
    }

    //cart

    // add to cart
    fun addToCart(cart: Cart, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to cart.clientId,
            "productId" to cart.productId,
            "quantity" to cart.quantity,
            "totalPrice" to cart.totalPrice,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("cart").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to cart") }
    }

    // get cart by client
    fun getCart(clientId: String): Flow<List<Cart>> = callbackFlow {
        val listener = firestore.collection("cart")
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
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0
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
        firestore.collection("cart").document(cartId)
            .update(mapOf("quantity" to quantity, "totalPrice" to totalPrice))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update cart") }
    }

    // delete from cart
    fun removeFromCart(cartId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("cart").document(cartId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from cart") }
    }

    //favorites

    // add to favorites
    fun addToFavorites(favorite: Favorites, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to favorite.clientId,
            "productId" to favorite.productId,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("favorites").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to favorites") }
    }

    // get favorites by client
    fun getFavorites(clientId: String): Flow<List<Favorites>> = callbackFlow {
        val listener = firestore.collection("favorites")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Favorites(
                        favoriteId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // delete from favorites
    fun removeFromFavorites(
        favoriteId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("favorites").document(favoriteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from favorites") }
    }

    //support tickets

    // add ticket
    fun addSupportTicket(
        ticket: SupportTickets,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "clientId" to ticket.clientId,
            "adminId" to ticket.adminId,
            "subject" to ticket.subject,
            "message" to ticket.message,
            "status" to ticket.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("support_tickets").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add ticket") }
    }

    // get all tickets
    fun getSupportTickets(): Flow<List<SupportTickets>> = callbackFlow {
        val listener = firestore.collection("support_tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    SupportTickets(
                        ticketId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        adminId = doc.getString("adminId") ?: "",
                        subject = doc.getString("subject") ?: "",
                        message = doc.getString("message") ?: "",
                        status = doc.getString("status") ?: "open"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // edit ticket status
    fun updateTicketStatus(
        ticketId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("support_tickets").document(ticketId)
            .update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update ticket") }
    }

    // delete ticket
    fun deleteSupportTicket(ticketId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("support_tickets").document(ticketId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete ticket") }
    }

    //customization request
    // add request
    fun addCustomizationRequest(
        request: CustomizationRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "clientId" to request.clientId,
            "adminId" to request.adminId,
            "productId" to request.productId,
            "details" to request.details,
            "colorPreference" to request.colorPreference,
            "sizePreference" to request.sizePreference,
            "status" to request.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("customization_requests").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add request") }
    }

    //get all requests
    fun getCustomizationRequests(): Flow<List<CustomizationRequest>> = callbackFlow {
        val listener = firestore.collection("customization_requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    CustomizationRequest(
                        requestId = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        adminId = doc.getString("adminId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        details = doc.getString("details") ?: "",
                        colorPreference = doc.getString("colorPreference") ?: "",
                        sizePreference = doc.getString("sizePreference") ?: "",
                        status = doc.getString("status") ?: "pending"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // edit request status
    fun updateCustomizationStatus(
        requestId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("customization_requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update request") }
    }

    // delete request
    fun deleteCustomizationRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("customization_requests").document(requestId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete request") }
    }
}