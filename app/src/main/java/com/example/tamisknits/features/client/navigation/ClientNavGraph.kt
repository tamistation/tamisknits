package com.example.tamisknits.features.client.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.tamisknits.features.client.cart.CartPage
import com.example.tamisknits.features.client.catalog.CatalogPage
import com.example.tamisknits.features.client.checkout.CheckoutPage
import com.example.tamisknits.features.client.checkout.OrderPlacedPage
import com.example.tamisknits.features.client.customization.list.CustomRequestsPage
import com.example.tamisknits.features.client.customization.request.CustomRequestPage
import com.example.tamisknits.features.client.favorites.FavoritesPage
import com.example.tamisknits.features.client.home.HomePage
import com.example.tamisknits.features.client.orders.details.OrderDetailsPage
import com.example.tamisknits.features.client.orders.list.OrdersPage
import com.example.tamisknits.features.client.productdetails.ProductDetailsPage
import com.example.tamisknits.features.client.profile.addresses.AddressesPage
import com.example.tamisknits.features.client.profile.edit.EditProfilePage
import com.example.tamisknits.features.client.profile.main.ProfilePage
import com.example.tamisknits.features.account.password.ChangePasswordPage
import com.example.tamisknits.features.client.support.chat.SupportChatPage
import com.example.tamisknits.features.client.support.create.CreateTicketPage
import com.example.tamisknits.features.client.support.list.SupportTicketsPage
import com.example.tamisknits.features.client.tracking.TrackingPage

private const val SLIDE_MILLIS = 320
private const val FADE_MILLIS = 220

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.currentClientTab != null && targetState.currentClientTab != null

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

fun NavHostController.navigateToTab(tab: ClientRoute, restore: Boolean = true) {
    navigate(tab) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = restore
    }
}

@Composable
fun ClientNavGraph(
    navController: NavHostController,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ClientRoute.Home,
        modifier = modifier,
        enterTransition = { enter() },
        exitTransition = { exit() },
        popEnterTransition = { popEnter() },
        popExitTransition = { popExit() }
    ) {
        composable<ClientRoute.Home> {
            HomePage(
                viewModel = hiltViewModel(),
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) },
                // Catalog arguments only apply to a fresh entry, so these skip restoring the saved tab.
                onSearchClick = {
                    navController.navigateToTab(
                        ClientRoute.Catalog(focusSearch = true),
                        restore = false
                    )
                },
                onCategoryClick = {
                    navController.navigateToTab(
                        ClientRoute.Catalog(category = it),
                        restore = false
                    )
                },
                onBrowseAllClick = {
                    navController.navigateToTab(
                        ClientRoute.Catalog(),
                        restore = false
                    )
                },
                onCustomBagClick = { navController.navigate(ClientRoute.RequestCustomBag()) }
            )
        }

        composable<ClientRoute.Catalog> {
            CatalogPage(
                viewModel = hiltViewModel(),
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) }
            )
        }

        composable<ClientRoute.Favorites> {
            FavoritesPage(
                viewModel = hiltViewModel(),
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) },
                onBrowseClick = { navController.navigateToTab(ClientRoute.Catalog()) }
            )
        }

        composable<ClientRoute.Cart> {
            CartPage(
                viewModel = hiltViewModel(),
                onCheckout = { navController.navigate(ClientRoute.Checkout) },
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) },
                onBrowseClick = { navController.navigateToTab(ClientRoute.Catalog()) }
            )
        }

        composable<ClientRoute.Profile> {
            ProfilePage(
                viewModel = hiltViewModel(),
                onEditProfile = { navController.navigate(ClientRoute.EditProfile) },
                onOrders = { navController.navigate(ClientRoute.Orders) },
                onAddresses = { navController.navigate(ClientRoute.Addresses) },
                onCustomRequests = { navController.navigate(ClientRoute.CustomRequests) },
                onSupport = { navController.navigate(ClientRoute.SupportTickets) },
                onChangePassword = { navController.navigate(ClientRoute.ChangePassword) },
                onLoggedOut = onLoggedOut
            )
        }

        composable<ClientRoute.ProductDetails> {
            ProductDetailsPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) },
                onViewCart = { navController.navigateToTab(ClientRoute.Cart) },
                onRequestCustom = { navController.navigate(ClientRoute.RequestCustomBag(it)) }
            )
        }

        composable<ClientRoute.Checkout> {
            CheckoutPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onOrderPlaced = { orderId ->
                    navController.navigate(ClientRoute.OrderPlaced(orderId)) {
                        popUpTo<ClientRoute.Checkout> { inclusive = true }
                    }
                }
            )
        }

        composable<ClientRoute.OrderPlaced> { entry ->
            val orderId = entry.toRoute<ClientRoute.OrderPlaced>().orderId
            OrderPlacedPage(
                orderId = orderId,
                onTrackOrder = {
                    navController.navigate(ClientRoute.OrderTracking(orderId)) {
                        popUpTo<ClientRoute.OrderPlaced> { inclusive = true }
                    }
                },
                onContinueShopping = { navController.navigateToTab(ClientRoute.Home) }
            )
        }

        composable<ClientRoute.Orders> {
            OrdersPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onOrderClick = { navController.navigate(ClientRoute.OrderDetails(it)) },
                onBrowseClick = { navController.navigateToTab(ClientRoute.Catalog()) }
            )
        }

        composable<ClientRoute.OrderDetails> {
            OrderDetailsPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onTrackOrder = { navController.navigate(ClientRoute.OrderTracking(it)) },
                onProductClick = { navController.navigate(ClientRoute.ProductDetails(it)) },
                onGetHelp = { navController.navigate(ClientRoute.CreateTicket(orderId = it)) }
            )
        }

        composable<ClientRoute.OrderTracking> {
            TrackingPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onOrderDetails = { orderId ->
                    // Coming from the details screen, going "to details" should just go back.
                    val cameFromDetails = navController.previousBackStackEntry
                        ?.destination?.hasRoute<ClientRoute.OrderDetails>() == true
                    if (cameFromDetails) navController.popBackSafely()
                    else navController.navigate(ClientRoute.OrderDetails(orderId))
                },
                onGetHelp = { navController.navigate(ClientRoute.CreateTicket(orderId = it)) }
            )
        }

        composable<ClientRoute.RequestCustomBag> {
            CustomRequestPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onSubmitted = {
                    navController.navigate(ClientRoute.CustomRequests) {
                        popUpTo<ClientRoute.RequestCustomBag> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<ClientRoute.CustomRequests> {
            CustomRequestsPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onNewRequest = { navController.navigate(ClientRoute.RequestCustomBag()) }
            )
        }

        composable<ClientRoute.SupportTickets> {
            SupportTicketsPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onTicketClick = { navController.navigate(ClientRoute.SupportChat(it)) },
                onNewTicket = { navController.navigate(ClientRoute.CreateTicket()) }
            )
        }

        composable<ClientRoute.CreateTicket> {
            CreateTicketPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely,
                onCreated = { ticketId ->
                    navController.navigate(ClientRoute.SupportChat(ticketId)) {
                        popUpTo<ClientRoute.CreateTicket> { inclusive = true }
                    }
                }
            )
        }

        composable<ClientRoute.SupportChat> {
            SupportChatPage(
                viewModel = hiltViewModel(),
                onBack = navController::popBackSafely
            )
        }

        composable<ClientRoute.EditProfile> {
            EditProfilePage(viewModel = hiltViewModel(), onBack = navController::popBackSafely)
        }

        composable<ClientRoute.Addresses> {
            AddressesPage(viewModel = hiltViewModel(), onBack = navController::popBackSafely)
        }

        composable<ClientRoute.ChangePassword> {
            ChangePasswordPage(viewModel = hiltViewModel(), onBack = navController::popBackSafely)
        }
    }
}
