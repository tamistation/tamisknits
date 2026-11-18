package com.example.tamisknits.features.client.customization.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.common.ClientUserUseCase
import com.example.tamisknits.features.client.customization.CustomizationUseCase
import com.example.tamisknits.features.client.navigation.ClientRoute
import com.example.tamisknits.models.CustomizationOptions
import com.example.tamisknits.models.CustomizationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CustomRequestViewModel @Inject constructor(
    private val useCase: CustomizationUseCase,
    userUseCase: ClientUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid = userUseCase.currentUid
    private val productId =
        savedStateHandle.toRoute<ClientRoute.RequestCustomBag>().productId.orEmpty()

    private val _uiState = MutableStateFlow(CustomRequestUiState(productId = productId))
    val uiState: StateFlow<CustomRequestUiState> = _uiState

    init {
        if (productId.isNotBlank()) {
            viewModelScope.launch {
                val product = useCase.getProduct(productId) ?: return@launch
                // The product's own colours go first so "this bag, but in..." is the obvious choice.
                val colors =
                    (product.colors + CustomizationOptions.colors).distinctBy { it.lowercase() }
                _uiState.update { it.copy(productName = product.name, colorOptions = colors) }
            }
        }
    }

    fun onColorSelected(color: String) {
        _uiState.update {
            it.copy(
                color = color,
                fieldErrors = it.fieldErrors - CustomRequestField.COLOR
            )
        }
    }

    fun onSizeSelected(size: String) {
        _uiState.update {
            it.copy(
                size = size,
                fieldErrors = it.fieldErrors - CustomRequestField.SIZE
            )
        }
    }

    fun onDetailsChange(details: String) {
        _uiState.update {
            it.copy(
                details = details.take(CustomRequestUiState.MAX_DETAILS_LENGTH),
                fieldErrors = it.fieldErrors - CustomRequestField.DETAILS
            )
        }
    }

    fun onReferenceLinkChange(link: String) {
        _uiState.update {
            it.copy(
                referenceLink = link.trim(),
                fieldErrors = it.fieldErrors - CustomRequestField.LINK
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val errors = buildMap {
            if (state.color.isBlank()) put(CustomRequestField.COLOR, "Pick a colour")
            if (state.size.isBlank()) put(CustomRequestField.SIZE, "Pick a size")
            if (state.details.trim().length < CustomRequestUiState.MIN_DETAILS_LENGTH) {
                put(
                    CustomRequestField.DETAILS,
                    "Tell us a little more (at least ${CustomRequestUiState.MIN_DETAILS_LENGTH} characters)"
                )
            }
            if (!useCase.isValidLink(state.referenceLink)) {
                put(CustomRequestField.LINK, "That doesn't look like a valid link")
            }
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, fieldErrors = emptyMap()) }
        useCase.submitRequest(
            request = CustomizationRequest(
                clientId = uid,
                productId = productId,
                details = state.details.trim(),
                colorPreference = state.color,
                sizePreference = state.size
            ),
            referenceLink = state.referenceLink,
            onSuccess = { requestId ->
                _uiState.update { it.copy(isSubmitting = false, submittedRequestId = requestId) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isSubmitting = false, message = error) }
            }
        )
    }

    fun consumeSubmitted() {
        _uiState.update { it.copy(submittedRequestId = null) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
