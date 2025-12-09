package com.example.gametrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        composable("login") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("signup") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            SignUpScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("home") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("addGame") {
            val viewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(context)
            )
            AddGameScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

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
