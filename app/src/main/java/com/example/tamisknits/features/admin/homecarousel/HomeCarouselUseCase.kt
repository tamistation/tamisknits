package com.example.tamisknits.features.admin.homecarousel

import com.example.tamisknits.models.Products
import com.example.tamisknits.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeCarouselUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    fun getProducts(): Flow<List<Products>> = repository.getProducts()

    fun getCarouselIds(): Flow<List<String>> = repository.getHomeCarouselIds()

    fun saveCarouselIds(
        productIds: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.setHomeCarouselIds(productIds, onSuccess, onFailure)
    }
}
