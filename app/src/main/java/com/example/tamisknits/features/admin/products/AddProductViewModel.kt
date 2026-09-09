package com.example.tamisknits.features.admin.products

import androidx.lifecycle.ViewModel
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val useCase: ProductManagementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun onCustomizableChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isCustomizable = value
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
        val variants = _uiState.value.variants.toMutableList()

        if (variants.size <= 1) return

        variants.removeAt(index)

        _uiState.value = _uiState.value.copy(
            variants = variants
        )
    }

    fun updateVariant(
        index: Int,
        variant: ProductVariant
    ) {
        val variants = _uiState.value.variants.toMutableList()

        variants[index] = variant

        _uiState.value = _uiState.value.copy(
            variants = variants
        )
    }

    fun addProduct(
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
            isLoading = true,
            errorMessage = null
        )

        val product = Products(
            name = state.name.trim(),
            description = state.description.trim(),
            category = state.category,
            isCustomizable = state.isCustomizable,
            variants = state.variants
        )

        useCase.addProduct(
            product = product,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
                onSuccess()
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        )
    }

    fun onImageSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}