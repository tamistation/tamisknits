package com.example.tamisknits.features.client.customization

import android.util.Patterns
import com.example.tamisknits.models.ClientCustomizationRequest
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.CustomizationRequest
import com.example.tamisknits.repository.ClientCustomizationRepository
import com.example.tamisknits.repository.ClientProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CustomizationUseCase @Inject constructor(
    private val customizationRepository: ClientCustomizationRepository,
    private val productRepository: ClientProductRepository
) {
    fun getRequests(clientId: String): Flow<ClientResult<List<ClientCustomizationRequest>>> =
        customizationRepository.getRequests(clientId)

    fun getProducts(): Flow<ClientResult<List<ClientProduct>>> = productRepository.getProducts()

    suspend fun getProduct(productId: String): ClientProduct? {
        val result = productRepository.getProduct(productId).first { it !is ClientResult.Loading }
        return (result as? ClientResult.Success)?.data
    }

    fun submitRequest(
        request: CustomizationRequest,
        referenceLink: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = customizationRepository.submitRequest(
        request,
        normalizeLink(referenceLink),
        onSuccess,
        onFailure
    )

    fun isValidLink(link: String): Boolean {
        val trimmed = link.trim()
        return trimmed.isEmpty() || Patterns.WEB_URL.matcher(normalizeLink(trimmed)).matches()
    }

    // People usually paste "instagram.com/p/..." without a scheme; store it as an openable URL.
    fun normalizeLink(link: String): String {
        val trimmed = link.trim()
        if (trimmed.isEmpty()) return ""
        return if (trimmed.startsWith("http://", true) || trimmed.startsWith(
                "https://",
                true
            )
        ) trimmed
        else "https://$trimmed"
    }
}
