package com.example.tamisknits.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

sealed interface AdminRoute {
    //closed list
    @Serializable
    data object Orders : AdminRoute
    @Serializable
    data object Support : AdminRoute
    @Serializable
    data object Settings : AdminRoute

    @Serializable
    data object Products : AdminRoute
    @Serializable
    data class TicketChat(val ticketId: String) : AdminRoute

    @Serializable
    data class OrderSummary(val orderId: String) : AdminRoute
}

val NavBackStackEntry.currentAdminRoute: AdminRoute?
    @SuppressLint("RestrictedApi")
    get() = when {
        destination.hasRoute<AdminRoute.Orders>() -> toRoute<AdminRoute.Orders>()
        destination.hasRoute<AdminRoute.Products>() -> toRoute<AdminRoute.Products>()
        destination.hasRoute<AdminRoute.Support>() -> toRoute<AdminRoute.Support>()
        destination.hasRoute<AdminRoute.Settings>() -> toRoute<AdminRoute.Settings>()
        destination.hasRoute<AdminRoute.TicketChat>() -> toRoute<AdminRoute.TicketChat>()
        destination.hasRoute<AdminRoute.OrderSummary>() -> toRoute<AdminRoute.OrderSummary>()
        else -> null
    }