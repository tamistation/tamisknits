package com.example.tamisknits.features.client.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

sealed interface ClientRoute {
    @Serializable
    data object Home : ClientRoute

    @Serializable
    data class Catalog(
        val category: String? = null,
        val focusSearch: Boolean = false
    ) : ClientRoute

    @Serializable
    data object Favorites : ClientRoute

    @Serializable
    data object Cart : ClientRoute

    @Serializable
    data object Profile : ClientRoute

    @Serializable
    data class ProductDetails(val productId: String) : ClientRoute

    @Serializable
    data object Checkout : ClientRoute

    @Serializable
    data class OrderPlaced(val orderId: String) : ClientRoute

    @Serializable
    data object Orders : ClientRoute

    @Serializable
    data class OrderDetails(val orderId: String) : ClientRoute

    @Serializable
    data class OrderTracking(val orderId: String) : ClientRoute

    @Serializable
    data class RequestCustomBag(val productId: String? = null) : ClientRoute

    @Serializable
    data object CustomRequests : ClientRoute

    @Serializable
    data object SupportTickets : ClientRoute

    @Serializable
    data class CreateTicket(val orderId: String? = null) : ClientRoute

    @Serializable
    data class SupportChat(val ticketId: String) : ClientRoute

    @Serializable
    data object EditProfile : ClientRoute

    @Serializable
    data object Addresses : ClientRoute

    @Serializable
    data object ChangePassword : ClientRoute
}

val NavBackStackEntry.currentClientTab: ClientRoute?
    @SuppressLint("RestrictedApi")
    get() = when {
        destination.hasRoute<ClientRoute.Home>() -> ClientRoute.Home
        destination.hasRoute<ClientRoute.Catalog>() -> toRoute<ClientRoute.Catalog>()
        destination.hasRoute<ClientRoute.Favorites>() -> ClientRoute.Favorites
        destination.hasRoute<ClientRoute.Cart>() -> ClientRoute.Cart
        destination.hasRoute<ClientRoute.Profile>() -> ClientRoute.Profile
        else -> null
    }
