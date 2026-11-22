package com.example.tamisknits.features.admin.homecarousel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamisknits.models.HomeCarousel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeCarouselViewModel @Inject constructor(
    private val useCase: HomeCarouselUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeCarouselUiState())
    val uiState: StateFlow<HomeCarouselUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                useCase.getProducts(),
                useCase.getCarouselIds()
            ) { products, ids -> products to ids }
                .collect { (products, savedIds) ->
                    _uiState.update { state ->
                        // Keep unsaved edits; otherwise follow what's stored.
                        val selected =
                            if (state.isLoading || !state.hasChanges) savedIds else state.selectedIds
                        state.copy(
                            isLoading = false,
                            allProducts = products,
                            savedIds = savedIds,
                            selectedIds = selected
                        )
                    }
                }
        }
    }

    fun add(productId: String) = _uiState.update { state ->
        if (state.isFull || productId in state.selectedIds) state
        else state.copy(selectedIds = state.selectedIds + productId)
    }

    fun remove(productId: String) = _uiState.update { state ->
        state.copy(selectedIds = state.selectedIds - productId)
    }

    fun moveUp(productId: String) = move(productId, -1)

    fun moveDown(productId: String) = move(productId, +1)

    // Moves within the visible list, so ids of deleted products never block a swap.
    private fun move(productId: String, offset: Int) = _uiState.update { state ->
        val ids = state.selectedProducts.map { it.productId }.toMutableList()
        val from = ids.indexOf(productId)
        val to = from + offset
        if (from == -1 || to !in ids.indices) return@update state
        ids.add(to, ids.removeAt(from))
        state.copy(selectedIds = ids)
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        // Deleted products are dropped from what gets stored.
        val ids = state.selectedProducts.map { it.productId }.take(HomeCarousel.MAX_PRODUCTS)
        _uiState.update { it.copy(isSaving = true) }
        useCase.saveCarouselIds(
            productIds = ids,
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedIds = ids,
                        selectedIds = ids,
                        message = "Home carousel updated"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isSaving = false, message = error) }
            }
        )
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
