package com.example.tamisknits.features.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.tamisknits.features.admin.orders.OrderManagementPage
import com.example.tamisknits.features.admin.ordersummary.OrderSummaryPage
import com.example.tamisknits.features.admin.products.add.AddProductPage
import com.example.tamisknits.features.admin.products.edit.EditProductPage
import com.example.tamisknits.features.admin.products.productmanagement.ProductManagementPage
import com.example.tamisknits.features.admin.settings.SettingsPage
import com.example.tamisknits.features.admin.support.chat.TicketChatPage
import com.example.tamisknits.features.admin.support.list.SupportPage
import com.example.tamisknits.navigation.AdminRoute
import com.example.tamisknits.navigation.currentAdminRoute


@Composable
fun AdminRootPage(onLoggedOut: () -> Unit) {
    val innerNavController: NavHostController = rememberNavController()
    val backStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.currentAdminRoute
    val isTopLevel = currentRoute is AdminRoute.Orders ||
            currentRoute is AdminRoute.Products ||
            currentRoute is AdminRoute.Support ||
            currentRoute is AdminRoute.Settings

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                AdminBottomNav(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        innerNavController.navigate(route) {
                            popUpTo(innerNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            NavHost(
                navController = innerNavController,
                startDestination = AdminRoute.Orders,
            ) {
                composable<AdminRoute.Orders> {
                    OrderManagementPage(
                        viewModel = hiltViewModel(),
                        onOrderClick = { orderId ->
                            innerNavController.navigate(
                                AdminRoute.OrderSummary(
                                    orderId
                                )
                            )
                        })
                }
                composable<AdminRoute.Products> {
                    ProductManagementPage(
                        viewModel = hiltViewModel(),
                        onAddProductClick = {
                            innerNavController.navigate(
                                AdminRoute.AddProduct
                            )
                        },
                        onEditProductClick = { productId ->
                            innerNavController.navigate(
                                AdminRoute.EditProduct(productId)
                            )
                        }
                    )
                }
                composable<AdminRoute.AddProduct> {
                    AddProductPage(
                        viewModel = hiltViewModel(),
                        onProductAdded = {
                            innerNavController.popBackStack()
                        },
                        onBackClick = {
                            innerNavController.popBackStack()
                        }
                    )
                }

                composable<AdminRoute.EditProduct> { backStackEntry ->
                    val route = backStackEntry.toRoute<AdminRoute.EditProduct>()

                    EditProductPage(
                        productId = route.productId,
                        viewModel = hiltViewModel(),
                        onProductUpdated = {
                            innerNavController.popBackStack()
                        },
                        onBackClick = {
                            innerNavController.popBackStack()
                        }
                    )
                }

                composable<AdminRoute.OrderSummary> { backStackEntry ->
                    val route = backStackEntry.toRoute<AdminRoute.OrderSummary>()
                    OrderSummaryPage(
                        orderId = route.orderId,
                        viewModel = hiltViewModel(),
                        onBackClick = { innerNavController.popBackStack() }
                    )
                }



                composable<AdminRoute.Support> {
                    SupportPage(
                        viewModel = hiltViewModel(),
                        onOpenTicket = { ticketId, _, _ ->
                            innerNavController.navigate(AdminRoute.TicketChat(ticketId))
                        }
                    )
                }

                composable<AdminRoute.TicketChat> {
                    TicketChatPage(
                        viewModel = hiltViewModel(),
                        onBack = { innerNavController.popBackStack() },
                        onClosed = { innerNavController.popBackStack() },
                    )
                }

                composable<AdminRoute.Settings> {
                    SettingsPage(onLoggedOut = onLoggedOut)
                }
            }
        }
    }
}