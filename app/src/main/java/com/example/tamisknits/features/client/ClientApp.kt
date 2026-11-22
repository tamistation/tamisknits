package com.example.tamisknits.features.client

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tamisknits.features.client.navigation.ClientBottomNav
import com.example.tamisknits.features.client.navigation.ClientNavGraph
import com.example.tamisknits.features.client.navigation.currentClientTab
import com.example.tamisknits.features.client.navigation.navigateToTab
import com.example.tamisknits.theme.AppColors

@Composable
fun ClientApp(
    onLoggedOut: () -> Unit,
    viewModel: ClientAppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.currentClientTab
    val cartCount by viewModel.cartCount.collectAsState()

    Scaffold(
        containerColor = AppColors.Surface,
        bottomBar = {
            AnimatedVisibility(
                visible = currentTab != null,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(160))
            ) {
                ClientBottomNav(
                    currentTab = currentTab,
                    cartCount = cartCount,
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
            ClientNavGraph(navController = navController, onLoggedOut = onLoggedOut)
        }
    }
}
