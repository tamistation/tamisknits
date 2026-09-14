package com.example.tamisknits.features.admin.products.productmanagement

import com.example.tamisknits.models.Products
import com.example.tamisknits.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductManagementUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {
    fun getProducts(): Flow<List<Products>> =
        repository.getProducts()

    fun getProduct(
        productId: String,
        onSuccess: (Products) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.getProduct(
            productId,
            onSuccess,
            onFailure
        )
    }


    fun addProduct(
        product: Products,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.addProduct(
            product,
            onSuccess,
            onFailure
        )
    }

    fun updateProduct(
        productId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.updateProduct(
            productId,
            updates,
            onSuccess,
            onFailure
        )
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.deleteProduct(
            productId,
            onSuccess,
            onFailure
        )
    }
}