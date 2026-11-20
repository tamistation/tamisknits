package com.example.tamisknits.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tamisknits.features.admin.navigation.AdminRootPage
import com.example.tamisknits.features.authentication.login.LoginPage
import com.example.tamisknits.features.authentication.onboarding.OnboardingScreen
import com.example.tamisknits.features.authentication.onboarding.OnboardingViewModel
import com.example.tamisknits.features.authentication.register.RegisterPage
import com.example.tamisknits.features.authentication.splash.SplashScreen
import com.example.tamisknits.features.client.ClientApp
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

            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsState()

            var splashFinished by remember {
                mutableStateOf(false)
            }

            SplashScreen(
                onFinished = {
                    splashFinished = true
                }
            )

            LaunchedEffect(splashFinished, onboardingCompleted) {
                if (splashFinished) {
                    val destination = if (onboardingCompleted) {
                        AppRoute.Login
                    } else {
                        AppRoute.Onboarding
                    }

                    navController.navigate(destination) {
                        popUpTo(AppRoute.Splash) {
                            inclusive = true
                        }
                    }
                }
            }
        }

        composable<AppRoute.Onboarding> {

            val onboardingViewModel: OnboardingViewModel = hiltViewModel()

            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinished = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.Onboarding) {
                            inclusive = true
                        }
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
            ClientApp(
                onLoggedOut = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.DeliveryHome> {
            DeliveryHomePage()
        }
    }
}