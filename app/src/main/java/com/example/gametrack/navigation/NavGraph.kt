package com.example.gametrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gametrack.GameViewModel
import com.example.gametrack.GameViewModelFactory
import com.example.gametrack.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // ========== PANTALLA DE LOGIN ==========
        composable("login") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // ========== PANTALLA DE REGISTRO ==========
        composable("signup") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            SignUpScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // ========== ¡NUEVO! RECUPERAR CONTRASEÑA ==========
        composable("forgotPassword") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            ForgotPasswordScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // ========== ¡NUEVO! RESTABLECER CONTRASEÑA ==========
        composable(
            route = "resetPassword/{email}/{token}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            ResetPasswordScreen(
                navController = navController,
                viewModel = viewModel,
                email = email,
                token = token
            )
        }

        // ========== PANTALLA PRINCIPAL ==========
        composable("home") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // ========== AÑADIR JUEGO ==========
        composable("addGame") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            AddGameScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // ========== PERFIL DE USUARIO ==========
        composable("profile") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            ProfileScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}