package com.example.tamisknits.features.delivery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tamisknits.features.delivery.navigation.DeliveryBottomNav
import com.example.tamisknits.features.delivery.navigation.DeliveryNavGraph
import com.example.tamisknits.features.delivery.navigation.currentDeliveryTab
import com.example.tamisknits.features.delivery.navigation.navigateToTab
import com.example.tamisknits.theme.AppColors

// Root of the delivery driver app; mirrors ClientApp (bottom bar only on the tab screens).
@Composable
fun DeliveryApp(onLoggedOut: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.currentDeliveryTab

    Scaffold(
        containerColor = AppColors.Surface,
        bottomBar = {
            AnimatedVisibility(
                visible = currentTab != null,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(160))
            ) {
                DeliveryBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab -> navController.navigateToTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            DeliveryNavGraph(navController = navController, onLoggedOut = onLoggedOut)
        }
    }
}
