package com.example.tamisknits.features.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.Products
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductManagementViewModel @Inject constructor(
    private val useCase: ProductManagementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductManagementUiState())
    val uiState: StateFlow<ProductManagementUiState> = _uiState

    private val productsFlow = useCase.getProducts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val filteredProducts: StateFlow<List<Products>> = combine(
        _uiState,
        productsFlow
    ) { state, products ->

        val categoryFiltered =
            if (state.selectedCategory == "All") {
                products
            } else {
                products.filter {
                    it.category.equals(
                        state.selectedCategory,
                        ignoreCase = true
                    )
                }
            }

        if (state.searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter {
                it.name.contains(
                    state.searchQuery,
                    ignoreCase = true
                )
            }
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            productsFlow.collect { products ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allProducts = products
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    fun onDeleteClick(productId: String) {
        _uiState.value = _uiState.value.copy(
            deleteDialogProductId = productId
        )
    }

    fun confirmDelete() {
        val productId = _uiState.value.deleteDialogProductId
            ?: return

        useCase.deleteProduct(
            productId = productId,
            onSuccess = {
                dismissDeleteDialog()
            },
            onFailure = {
                dismissDeleteDialog()
            }
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            deleteDialogProductId = null
        )
    }

    fun addProduct(
        product: Products,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        useCase.addProduct(
            product = product,
            onSuccess = {
                onSuccess()
            },
            onFailure = {
                onFailure(it)
            }
        )
    }

    fun updateProduct(
        productId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        useCase.updateProduct(
            productId = productId,
            updates = updates,
            onSuccess = {
                onSuccess()
            },
            onFailure = {
                onFailure(it)
            }
        )
    }
}