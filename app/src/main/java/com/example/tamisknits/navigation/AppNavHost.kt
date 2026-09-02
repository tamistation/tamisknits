package com.example.tamisknits.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tamisknits.features.admin.AdminRootPage
import com.example.tamisknits.features.authentication.login.LoginPage
import com.example.tamisknits.features.authentication.register.RegisterPage
import com.example.tamisknits.features.authentication.splash.SplashScreen
import com.example.tamisknits.features.client.ClientHomePage
import com.example.tamisknits.features.delivery.DeliveryHomePage

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash,
    ) {
        composable<AppRoute.Splash> {
            SplashScreen(
                onFinished = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Login> {
            LoginPage(
                viewModel = hiltViewModel(),
                onLoginSuccess = { user ->
                    val destination: AppRoute? = when (user.userType.type) {
                        "admin" -> AppRoute.AdminHome
                        "client" -> AppRoute.ClientHome
                        "delivery" -> AppRoute.DeliveryHome
                        else -> null
                    }
                    destination?.let {
                        navController.navigate(it) {
                            popUpTo(AppRoute.Login) { inclusive = true }
                        }
                    }
                },
                onRedirectToRegister = {
                    navController.navigate(AppRoute.Register)
                }
            )
        }

        composable<AppRoute.Register> {
            RegisterPage(
                viewModel = hiltViewModel(),
                onRegisterSuccess = { user ->
                    val destination: AppRoute? = when (user.userType.type) {
                        "client" -> AppRoute.ClientHome
                        "delivery" -> AppRoute.DeliveryHome
                        else -> null
                    }
                    destination?.let {
                        navController.navigate(it) {
                            popUpTo(AppRoute.Login) { inclusive = true }
                        }
                    }
                },
                onRedirectToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.AdminHome> {
            AdminRootPage(
                onLoggedOut = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        composable<AppRoute.ClientHome> {
            ClientHomePage()
        }

        composable<AppRoute.DeliveryHome> {
            DeliveryHomePage()
        }
    }
}