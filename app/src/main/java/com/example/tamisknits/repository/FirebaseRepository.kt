package com.example.tamisknits.repository

import com.example.tamisknits.models.Cart
import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.models.Delivery
import com.example.tamisknits.models.Favorites
import com.example.tamisknits.models.OrderStatusConfig
import com.example.tamisknits.models.Orders
import com.example.tamisknits.models.Permissions
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.models.TicketMessage
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


    fun addPermission(
        permission: Permissions,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "name" to permission.name,
            "description" to permission.description
        )
        firestore.collection("Permissions").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add permission") }
    }


    fun getPermissions(): Flow<List<Permissions>> = callbackFlow {
        val listener = firestore.collection("Permissions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Permissions(
                        permissionId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }


    fun getPermissionsByIds(
        ids: List<String>,
        onSuccess: (List<Permissions>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (ids.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        firestore.collection("Permissions")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Permissions(
                        permissionId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get permissions") }
    }

    // delete permission
    fun deletePermission(permissionId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Permissions").document(permissionId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete permission") }
    }


    fun addUserType(
        userType: UserType,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "type" to userType.type,
            "permissionIds" to userType.permissionIds
        )
        firestore.collection("UserType").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user type") }
    }

    // get user type
    fun getUserType(
        usertypeId: String,
        onSuccess: (UserType) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (usertypeId.isEmpty()) {
            onSuccess(UserType())
            return
        }
        firestore.collection("UserType").document(usertypeId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        UserType(
                            usertypeId = doc.id,
                            type = doc.getString("type") ?: "",
                            permissionIds = (doc.get("permissionIds") as? List<String>)
                                ?: emptyList()
                        )
                    )
                } else {
                    onFailure("User type not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user type") }
    }


    fun updateUserType(
        usertypeId: String,
        newType: String,
        newPermissionIds: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("UserType").document(usertypeId)
            .update(mapOf("type" to newType, "permissionIds" to newPermissionIds))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user type") }
    }

    // delete user type
    fun deleteUserType(usertypeId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("UserType").document(usertypeId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user type") }
    }


    // get current logged in user
    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("User")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            trySend(null); return@addSnapshotListener
                        }

                        if (document != null && document.exists()) {
                            val usertypeId = document.getString("usertypeId") ?: ""
                            getUserType(
                                usertypeId,
                                onSuccess = { userType ->
                                    val user = User(
                                        uid = document.id,
                                        name = document.getString("name") ?: "",
                                        email = document.getString("email") ?: "",
                                        phone = document.getString("phone") ?: "",
                                        userType = userType,
                                        status = document.getString("status") ?: "active"
                                    )
                                    trySend(user)
                                },
                                onFailure = {
                                    // still return the user even if usertype lookup fails,
                                    // just with an empty UserType
                                    trySend(
                                        User(
                                            uid = document.id,
                                            name = document.getString("name") ?: "",
                                            email = document.getString("email") ?: "",
                                            phone = document.getString("phone") ?: "",
                                            userType = UserType()
                                        )
                                    )
                                }
                            )
                        } else {
                            trySend(null)
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }


    fun addUser(
        user: User,
        usertypeId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "usertypeId" to usertypeId,
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to user.status,
        )
        firestore.collection("User").document(user.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user") }
    }


    fun getUser(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("User").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val usertypeId = doc.getString("usertypeId") ?: ""
                    getUserType(
                        usertypeId,
                        onSuccess = { userType ->
                            onSuccess(
                                User(
                                    uid = doc.id,
                                    name = doc.getString("name") ?: "",
                                    email = doc.getString("email") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    userType = userType,
                                    status = doc.getString("status") ?: "active"
                                )
                            )
                        },
                        onFailure = { onFailure(it) }
                    )
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user") }
    }


    fun getUsersByType(
        usertypeId: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        getUserType(usertypeId, onSuccess = { userType ->
            firestore.collection("User")
                .whereEqualTo("usertypeId", usertypeId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map { doc ->
                        User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            userType = userType,
                            status = doc.getString("status") ?: "active"
                        )
                    }
                    onSuccess(list)
                }
                .addOnFailureListener { onFailure(it.message ?: "Failed to get users") }
        }, onFailure = { onFailure(it) })
    }


    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("User").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user") }
    }

    // delete user
    fun deleteUser(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("User").document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user") }
    }


    fun addProduct(product: Products, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "name" to product.name,
            "description" to product.description,
            "category" to product.category,
            "isCustomizable" to product.isCustomizable,
            "imageUrl" to product.imageUrl,
            "variants" to product.variants.map { variant ->
                mapOf(
                    "variantId" to variant.variantId,
                    "size" to variant.size,
                    "color" to variant.color,
                    "stock" to variant.stock,
                    "price" to variant.price,
                    "imageUrl" to variant.imageUrl
                )
            },
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Products").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add product") }
    }

    fun getProducts(): Flow<List<Products>> = callbackFlow {
        val listener = firestore.collection("Products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Products(
                        productId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        isCustomizable = doc.getBoolean("isCustomizable") ?: false,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        variants = (doc.get("variants") as? List<Map<String, Any>>)?.map { v ->
                            ProductVariant(
                                variantId = v["variantId"] as? String ?: "",
                                size = v["size"] as? String ?: "",
                                color = v["color"] as? String ?: "",
                                stock = (v["stock"] as? Long)?.toInt() ?: 0,
                                price = (v["price"] as? Number)?.toDouble() ?: 0.0,
                                imageUrl = v["imageUrl"] as? String ?: ""
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // get single product
    fun getProduct(productId: String, onSuccess: (Products) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Products").document(productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        Products(
                            productId = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "",
                            isCustomizable = doc.getBoolean("isCustomizable") ?: false,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            variants = (doc.get("variants") as? List<Map<String, Any>>)?.map { v ->
                                ProductVariant(
                                    variantId = v["variantId"] as? String ?: "",
                                    size = v["size"] as? String ?: "",
                                    color = v["color"] as? String ?: "",
                                    stock = (v["stock"] as? Long)?.toInt() ?: 0,
                                    price = (v["price"] as? Number)?.toDouble() ?: 0.0,
                                    imageUrl = v["imageUrl"] as? String ?: ""
                                )
                            } ?: emptyList()
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
        firestore.collection("Products").document(productId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update product") }
    }

    // delete product
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Products").document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete product") }
    }


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

    //favorites

    // add to favorites
    fun addToFavorites(
        favorite: Favorites,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "clientId" to favorite.clientId,
            "productId" to favorite.productId,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Favorites").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add to favorites") }
    }

    // get favorites by client
    fun getFavorites(clientId: String): Flow<List<Favorites>> = callbackFlow {
        val listener = firestore.collection("Favorites")
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
        firestore.collection("Favorites").document(favoriteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to remove from favorites") }
    }


    // add ticket
    fun addSupportTicket(
        ticket: SupportTickets,
        onSuccess: (String) -> Unit,
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
        firestore.collection("SupportTickets").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add ticket") }
    }

    // get all tickets
    fun getSupportTickets(): Flow<List<SupportTickets>> = callbackFlow {
        val listener = firestore.collection("SupportTickets")
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
        firestore.collection("SupportTickets").document(ticketId)
            .update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update ticket") }
    }

    // delete ticket
    fun deleteSupportTicket(ticketId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("SupportTickets").document(ticketId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete ticket") }
    }


    // add request
    fun addCustomizationRequest(
        request: CustomizationRequest,
        onSuccess: (String) -> Unit,
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
        firestore.collection("CustomizationRequests").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add request") }
    }

    // get all requests
    fun getCustomizationRequests(): Flow<List<CustomizationRequest>> = callbackFlow {
        val listener = firestore.collection("CustomizationRequests")
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
        firestore.collection("CustomizationRequests").document(requestId)
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
        firestore.collection("CustomizationRequests").document(requestId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete request") }
    }

    // get tickets by client
    fun getTicketsByClient(clientId: String): Flow<List<SupportTickets>> = callbackFlow {
        val listener = firestore.collection("SupportTickets")
            .whereEqualTo("clientId", clientId)
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

    // add a chat message to a ticket
    fun addTicketMessage(
        msg: TicketMessage,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "ticketId" to msg.ticketId,
            "senderId" to msg.senderId,
            "senderType" to msg.senderType,
            "message" to msg.message,
            "sentAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("TicketMessages").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to send message") }
    }

    // live stream of messages for a ticket, ordered by time
    fun getTicketMessages(ticketId: String): Flow<List<TicketMessage>> = callbackFlow {
        val listener = firestore.collection("TicketMessages")
            .whereEqualTo("ticketId", ticketId)
            .orderBy("sentAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TicketMessages", "Query failed", error) // add this line
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    TicketMessage(
                        messageId = doc.id,
                        ticketId = doc.getString("ticketId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        senderType = doc.getString("senderType") ?: "",
                        message = doc.getString("message") ?: "",
                        sentAt = doc.getTimestamp("sentAt")?.seconds ?: 0L
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }


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

    fun getSupportTicket(
        ticketId: String,
        onSuccess: (SupportTickets) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("SupportTickets").document(ticketId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        SupportTickets(
                            ticketId = doc.id,
                            clientId = doc.getString("clientId") ?: "",
                            adminId = doc.getString("adminId") ?: "",
                            subject = doc.getString("subject") ?: "",
                            message = doc.getString("message") ?: "",
                            status = doc.getString("status") ?: "open"
                        )
                    )
                } else onFailure("Ticket not found")
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get ticket") }
    }
}