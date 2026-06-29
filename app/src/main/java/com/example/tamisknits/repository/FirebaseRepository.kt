package com.example.tamisknits.repository

import android.util.Log
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
                firestore.collection("User")
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
                                userType = UserType(
                                    usertypeiid = "",
                                    uid = document.id,
                                    type = document.getString("type") ?: "",
                                    permissions = (document.get("permissions") as? List<String>)
                                        ?: emptyList()
                                )
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
            "createdAt" to FieldValue.serverTimestamp(),
            "type" to user.userType.type,
            "permissions" to user.userType.permissions
        )
        firestore.collection("User").document(user.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user") }
    }

    // get single user
    fun getUser(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("User").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            userType = UserType(
                                usertypeiid = "",
                                uid = doc.id,
                                type = doc.getString("type") ?: "",
                                permissions = (doc.get("permissions") as? List<String>)
                                    ?: emptyList()
                            )
                        )
                    )
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user") }
    }

    // edit user
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

    //usertypes

    // add user type
    fun addUserType(userType: UserType, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "usertypeid" to userType.usertypeiid,
            "uid" to userType.uid,
            "type" to userType.type,
            "permissions" to userType.permissions
        )
        firestore.collection("UserType").document(userType.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user type") }
    }

    // get user type
    fun getUserType(uid: String, onSuccess: (UserType) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("UserType").document(uid)
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
        firestore.collection("UserType").document(uid)
            .update(mapOf("type" to newType, "permissions" to newPermissions))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user type") }
    }

    // delete user type
    fun deleteUserType(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("UserType").document(uid)
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
        firestore.collection("Products").add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add product") }
    }

    // get all products
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
        firestore.collection("Products").document(productId)
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

    //orders

    // add order
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
        firestore.collection("Orders").add(data)
            .addOnSuccessListener { onSuccess() }
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
        firestore.collection("Cart").add(data)
            .addOnSuccessListener { onSuccess() }
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
        firestore.collection("Cart").document(cartId)
            .update(mapOf("quantity" to quantity, "totalPrice" to totalPrice))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update cart") }
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
    fun addToFavorites(favorite: Favorites, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val data = hashMapOf(
            "clientId" to favorite.clientId,
            "productId" to favorite.productId,
            "addedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("Favorites").add(data)
            .addOnSuccessListener { onSuccess() }
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
        firestore.collection("SupportTickets").add(data)
            .addOnSuccessListener { onSuccess() }
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
        firestore.collection("CustomizationRequests").add(data)
            .addOnSuccessListener { onSuccess() }
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

    //testing part

    fun testUserFunction() {
        val testUser = User(
            uid = "test12",
            name = "admin Test",
            email = "admin@test.com",
            phone = "03222222",
            userType = UserType(
                usertypeiid = "type2",
                uid = "test12",
                type = "admin",
                permissions = listOf(
                    "add/edit/delete users",
                    "add/edit/delete products",
                    "view/respond/close support tickets",
                    "view/approve/reject customization requests",
                    "assign delivery",
                    "view/update/delete order"
                )
            )
        )

        addUser(
            testUser,
            onSuccess = {
                Log.d("TEST", "User added!")
                getUser(
                    "test12",
                    onSuccess = { fetchedUser ->
                        Log.d("TEST", "Fetched: $fetchedUser")
                        Log.d("TEST", "Name: ${fetchedUser.name}")
                        Log.d("TEST", "Type: ${fetchedUser.userType.type}")
                        Log.d("TEST", "Permissions: ${fetchedUser.userType.permissions}")
                    },
                    onFailure = { Log.e("TEST", "Get failed: $it") }
                )
            },
            onFailure = { Log.e("TEST", "Add failed: $it") }
        )
    }

    fun testProductFunctions() {
        val testProduct = Products(
            productId = "",
            name = "Crochet Bag",
            description = "Handmade tshirt yarn",
            price = 25.0,
            category = "bags",
            imageUrl = "https://test.com/image.jpg",
            stock = 10,
            isCustomizable = true
        )

        addProduct(
            testProduct,
            onSuccess = {
                Log.d("TEST", "Product added!")
                getProduct(
                    "",
                    onSuccess = { p -> Log.d("TEST", "Product fetched: ${p.name}") },
                    onFailure = { Log.e("TEST", "Get product failed: $it") }
                )
            },
            onFailure = { Log.e("TEST", "Add product failed: $it") }
        )
    }

    fun testOrderFunctions() {
        val testOrder = Orders(
            orderId = "",
            clientId = "test456",
            deliveryId = "delivery1",
            items = listOf(mapOf("productId" to "prod1", "quantity" to 2)),
            totalPrice = 50.0,
            status = "pending",
            shippingAddress = mapOf("city" to "Beirut", "street" to "Main St"),
            isCustomOrder = false
        )

        addOrder(
            testOrder,
            onSuccess = {
                Log.d("TEST", "Order added!")
                getOrdersByClient(
                    "test456",
                    onSuccess = { orders ->
                        Log.d("TEST", "Orders count: ${orders.size}")
                        orders.forEach {
                            Log.d(
                                "TEST",
                                "Order: ${it.orderId} status: ${it.status}"
                            )
                        }
                    },
                    onFailure = { Log.e("TEST", "Get orders failed: $it") }
                )
            },
            onFailure = { Log.e("TEST", "Add order failed: $it") }
        )
    }

    fun testCartFunctions() {
        val testCart = Cart(
            cartId = "",
            clientId = "test456",
            productId = "prod1",
            quantity = 2,
            totalPrice = 50.0
        )

        addToCart(
            testCart,
            onSuccess = {
                Log.d("TEST", "Cart item added!")
                Log.d("TEST", "Check Firestore Cart collection for test456")
            },
            onFailure = { Log.e("TEST", "Add to cart failed: $it") }
        )
    }

    fun testFavoritesFunctions() {
        val testFavorite = Favorites(
            favoriteId = "",
            clientId = "test456",
            productId = "prod1"
        )

        addToFavorites(
            testFavorite,
            onSuccess = {
                Log.d("TEST", "Favorite added!")
                Log.d("TEST", "Check Firestore Favorites collection for test456")
            },
            onFailure = { Log.e("TEST", "Add favorite failed: $it") }
        )
    }

    fun testSupportTicketFunctions() {
        val testTicket = SupportTickets(
            ticketId = "",
            clientId = "test456",
            adminId = "admin1",
            subject = "Where is my order?",
            message = "I placed an order 3 days ago and haven't heard back",
            status = "open"
        )

        addSupportTicket(
            testTicket,
            onSuccess = {
                Log.d("TEST", "Ticket added!")
                updateTicketStatus(
                    "", "resolved",
                    onSuccess = { Log.d("TEST", "Ticket status updated!") },
                    onFailure = { Log.e("TEST", "Update ticket failed: $it") }
                )
            },
            onFailure = { Log.e("TEST", "Add ticket failed: $it") }
        )
    }

    fun testCustomizationFunctions() {
        val testRequest = CustomizationRequest(
            requestId = "",
            clientId = "test456",
            adminId = "admin1",
            productId = "prod1",
            details = "I want a blue bag with long handles",
            colorPreference = "blue",
            sizePreference = "large",
            status = "pending"
        )

        addCustomizationRequest(
            testRequest,
            onSuccess = {
                Log.d("TEST", "Customization request added!")
                updateCustomizationStatus(
                    "", "approved",
                    onSuccess = { Log.d("TEST", "Request status updated!") },
                    onFailure = { Log.e("TEST", "Update request failed: $it") }
                )
            },
            onFailure = { Log.e("TEST", "Add request failed: $it") }
        )
    }
}