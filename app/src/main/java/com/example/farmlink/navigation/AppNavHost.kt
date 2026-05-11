package com.example.farmlink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.farmlink.ui.theme.screens.FeedScreen
import com.example.farmlink.ui.theme.screens.login.LoginScreen
import com.example.farmlink.ui.theme.screens.register.RegisterScreen
import com.example.farmlink.ui.theme.screens.profile.ProfileScreen
import com.example.farmlink.ui.theme.screens.addproduct.AddProductScreen
import com.example.farmlink.ui.theme.screens.updateproduct.UpdateProductScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    startDestination: String = ROUTE_FEED  // ✅ Feed is now the start
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {

        // ✅ Feed — no auth required
        composable(ROUTE_FEED) {
            FeedScreen(navController = navController)
        }

        // Login
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ROUTE_FEED) {
                        popUpTo(ROUTE_FEED) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) }
            )
        }

        // Register
        composable(ROUTE_REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_REGISTER) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.navigate(ROUTE_LOGIN) }
            )
        }

        // Profile — auth required (handled inside FeedBottomNav)
        composable(ROUTE_PROFILE) {
            ProfileScreen(navController = navController)
        }

        // Add Product — auth required (handled inside FeedBottomNav)
        composable(ROUTE_ADD_PRODUCT) {
            AddProductScreen(navController = navController)
        }

        // Update Product
        composable(
            route = ROUTE_UPDATE_PRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UpdateProductScreen(navController = navController, productId = productId)
        }
    }
}