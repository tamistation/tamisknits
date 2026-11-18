package com.example.tamisknits.features.client.common

import com.example.tamisknits.models.Favorites
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ClientFavoritesUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {
    fun getFavorites(clientId: String): Flow<List<Favorites>> =
        if (clientId.isBlank()) flowOf(emptyList()) else repository.getFavorites(clientId)

    // Firestore applies local writes to the listener immediately, so the heart flips without waiting on the network.
    fun toggleFavorite(
        clientId: String,
        productId: String,
        current: List<Favorites>,
        onFailure: (String) -> Unit
    ) {
        if (clientId.isBlank()) {
            onFailure("Please sign in again")
            return
        }
        val existing = current.filter { it.productId == productId }
        if (existing.isEmpty()) {
            repository.addToFavorites(
                Favorites(clientId = clientId, productId = productId),
                onSuccess = {},
                onFailure = onFailure
            )
        } else {
            existing.forEach { favorite ->
                repository.removeFromFavorites(
                    favorite.favoriteId,
                    onSuccess = {},
                    onFailure = onFailure
                )
            }
        }
    }
}
