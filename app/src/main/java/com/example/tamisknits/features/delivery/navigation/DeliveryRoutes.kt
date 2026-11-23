package com.example.tamisknits.features.delivery.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable

sealed interface DeliveryRoute {
    @Serializable
    data object Deliveries : DeliveryRoute

    @Serializable
    data object Profile : DeliveryRoute

    @Serializable
    data class Details(val orderId: String) : DeliveryRoute

    @Serializable
    data object ChangePassword : DeliveryRoute
}

val NavBackStackEntry.currentDeliveryTab: DeliveryRoute?
    @SuppressLint("RestrictedApi")
    get() = when {
        destination.hasRoute<DeliveryRoute.Deliveries>() -> DeliveryRoute.Deliveries
        destination.hasRoute<DeliveryRoute.Profile>() -> DeliveryRoute.Profile
        else -> null
    }
