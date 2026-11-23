package com.example.tamisknits.repository

import com.example.tamisknits.models.Favorites
import com.example.tamisknits.models.HomeCarousel
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class ProductRepository(
    private val firestore: FirebaseFirestore
) {


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

    fun getHomeCarouselIds(): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection(HomeCarousel.COLLECTION)
            .document(HomeCarousel.DOCUMENT)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val ids = (document?.get(HomeCarousel.FIELD_PRODUCT_IDS) as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    fun setHomeCarouselIds(
        productIds: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection(HomeCarousel.COLLECTION)
            .document(HomeCarousel.DOCUMENT)
            .set(
                mapOf(
                    HomeCarousel.FIELD_PRODUCT_IDS to productIds,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Couldn't save the carousel") }
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
}
