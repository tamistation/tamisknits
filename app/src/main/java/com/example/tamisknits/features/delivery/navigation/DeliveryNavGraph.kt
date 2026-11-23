package com.example.tamisknits.features.delivery.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tamisknits.features.account.password.ChangePasswordPage
import com.example.tamisknits.features.delivery.deliveries.DeliveriesPage
import com.example.tamisknits.features.delivery.details.DeliveryDetailsPage
import com.example.tamisknits.features.delivery.profile.DeliveryProfilePage

private const val DELIVERIES_MESSAGE_KEY = "deliveries_message"
private const val SLIDE_MILLIS = 320
private const val FADE_MILLIS = 220

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.currentDeliveryTab != null && targetState.currentDeliveryTab != null

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition =
    if (isTabSwitch()) fadeIn(tween(FADE_MILLIS))
    else slideInHorizontally(tween(SLIDE_MILLIS)) { it / 4 } + fadeIn(tween(SLIDE_MILLIS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition =
    if (isTabSwitch()) fadeOut(tween(FADE_MILLIS))
    else slideOutHorizontally(tween(SLIDE_MILLIS)) { -it / 10 } + fadeOut(tween(FADE_MILLIS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    if (isTabSwitch()) fadeIn(tween(FADE_MILLIS))
    else slideInHorizontally(tween(SLIDE_MILLIS)) { -it / 10 } + fadeIn(tween(SLIDE_MILLIS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    if (isTabSwitch()) fadeOut(tween(FADE_MILLIS))
    else slideOutHorizontally(tween(SLIDE_MILLIS)) { it / 4 } + fadeOut(tween(FADE_MILLIS))

// Ignores a second tap on "back" while the screen is already leaving, which would otherwise pop the root.
fun NavHostController.popBackSafely() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) popBackStack()
}

fun NavHostController.navigateToTab(tab: DeliveryRoute) {
    navigate(tab) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun DeliveryNavGraph(
    navController: NavHostController,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DeliveryRoute.Deliveries,
        modifier = modifier,
        enterTransition = { enter() },
        exitTransition = { exit() },
        popEnterTransition = { popEnter() },
        popExitTransition = { popExit() }
    ) {
        composable<DeliveryRoute.Deliveries> { entry ->
            // Set by the details screen right before it pops back here after a delivery.
            val message by entry.savedStateHandle
                .getStateFlow<String?>(DELIVERIES_MESSAGE_KEY, null)
                .collectAsState()
            DeliveriesPage(
                viewModel = hiltViewModel(),
                onOrderClick = { navController.navigate(DeliveryRoute.Details(it)) },
                message = message,
                onMessageShown = { entry.savedStateHandle[DELIVERIES_MESSAGE_KEY] = null }
            )
        }

        composable<DeliveryRoute.Details> {
            DeliveryDetailsPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onDelivered = {
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set(DELIVERIES_MESSAGE_KEY, "Delivered 🎉")
                    navController.popBackSafely()
                }
            )
        }

        composable<DeliveryRoute.Profile> {
            DeliveryProfilePage(
                viewModel = hiltViewModel(),
                onChangePassword = { navController.navigate(DeliveryRoute.ChangePassword) },
                onLoggedOut = onLoggedOut
            )
        }

        composable<DeliveryRoute.ChangePassword> {
            ChangePasswordPage(viewModel = hiltViewModel(), onBack = navController::popBackSafely)
        }
    }
}
