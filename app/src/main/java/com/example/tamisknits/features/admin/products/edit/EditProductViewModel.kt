package com.example.tamisknits.features.admin.products.edit

import androidx.lifecycle.ViewModel
import com.example.tamisknits.features.admin.products.productmanagement.ProductManagementUseCase
import com.example.tamisknits.models.ProductVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val useCase: ProductManagementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditProductUiState()
    )

    val uiState: StateFlow<EditProductUiState> = _uiState

    fun loadProduct(productId: String) {

        _uiState.value = _uiState.value.copy(
            productId = productId,
            isLoading = true,
            errorMessage = null
        )

        useCase.getProduct(
            productId = productId,
            onSuccess = { product ->
                _uiState.value = EditProductUiState(
                    productId = product.productId,
                    name = product.name,
                    description = product.description,
                    category = product.category,
                    isCustomizable = product.isCustomizable,
                    imageUri = product.imageUrl.ifEmpty { null },
                    variants = product.variants,
                    isLoading = false
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        )
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value
        )
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value
        )
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(
            category = value
        )
    }

    fun onCustomizableChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isCustomizable = value
        )
    }

    fun onImageSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri
        )
    }

    fun addVariant() {
        val newVariant = ProductVariant(
            variantId = UUID.randomUUID().toString()
        )

        _uiState.value = _uiState.value.copy(
            variants = _uiState.value.variants + newVariant
        )
    }

    fun removeVariant(index: Int) {

        val variants = _uiState.value
            .variants
            .toMutableList()

        if (variants.size <= 1) {
            return
        }

        variants.removeAt(index)

        _uiState.value = _uiState.value.copy(
            variants = variants
        )
    }

    fun updateVariant(
        index: Int,
        variant: ProductVariant
    ) {

        val variants = _uiState.value
            .variants
            .toMutableList()

        variants[index] = variant

        _uiState.value = _uiState.value.copy(
            variants = variants
        )
    }

    fun updateProduct(
        onSuccess: () -> Unit
    ) {

        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Product name is required"
            )
            return
        }

        if (state.variants.isEmpty()) {
            _uiState.value = state.copy(
                errorMessage = "At least one variant is required"
            )
            return
        }

        _uiState.value = state.copy(
            isSaving = true,
            errorMessage = null
        )

        val updates = hashMapOf<String, Any>(
            "name" to state.name.trim(),
            "description" to state.description.trim(),
            "category" to state.category,
            "isCustomizable" to state.isCustomizable,
            "imageUrl" to (state.imageUri ?: ""),
            "variants" to state.variants.map { variant ->
                mapOf(
                    "variantId" to variant.variantId,
                    "size" to variant.size,
                    "color" to variant.color,
                    "stock" to variant.stock,
                    "price" to variant.price,
                    "imageUrl" to variant.imageUrl
                )
            }
        )

        useCase.updateProduct(
            productId = state.productId,
            updates = updates,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isSaving = false
                )

                onSuccess()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error
                )
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}