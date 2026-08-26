package com.example.tamisknits.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Splash : AppRoute
    @Serializable
    data object Login : AppRoute
    @Serializable
    data object Register : AppRoute
    @Serializable
    data object AdminHome : AppRoute
    @Serializable
    data object ClientHome : AppRoute
    @Serializable
    data object DeliveryHome : AppRoute
}