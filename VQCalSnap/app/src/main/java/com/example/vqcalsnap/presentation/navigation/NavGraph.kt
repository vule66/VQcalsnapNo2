package com.example.vqcalsnap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import com.example.vqcalsnap.presentation.auth.LoginScreen
import com.example.vqcalsnap.presentation.auth.AuthViewModel
import com.example.vqcalsnap.presentation.camera.CameraScreen
import com.example.vqcalsnap.presentation.history.HistoryScreen
import com.example.vqcalsnap.presentation.profile.ProfileScreen
import com.example.vqcalsnap.presentation.result.ResultScreen

sealed class Screen(val route: String) {
    object AuthGate : Screen("auth_gate")
    object Login : Screen("login")
    object Profile : Screen("profile")
    object Camera : Screen("camera")
    object Result : Screen("result/{imagePath}") {
        fun createRoute(imagePath: String) = "result/$imagePath"
    }
    object History : Screen("history")
}

sealed class Graph(val route: String) {
    object Auth : Graph("auth_graph")
    object Main : Graph("main_graph")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavHost(
        navController = navController,
        startDestination = Screen.AuthGate.route
    ) {
        composable(Screen.AuthGate.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        navigation(
            startDestination = Screen.Login.route,
            route = Graph.Auth.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    uiState = authState,
                    onUsernameChanged = authViewModel::onUsernameChanged,
                    onPasswordChanged = authViewModel::onPasswordChanged,
                    onSubmit = authViewModel::submit,
                    onToggleMode = authViewModel::toggleMode
                )
            }
        }

        navigation(
            startDestination = Screen.Profile.route,
            route = Graph.Main.route
        ) {
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onLogout = authViewModel::logout
                )
            }
            composable(Screen.Camera.route) {
                CameraScreen(navController = navController)
            }
            composable(Screen.Result.route) { backStackEntry ->
                val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
                ResultScreen(
                    imagePath = imagePath,
                    navController = navController
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(navController = navController)
            }
        }
    }

    LaunchedEffect(authState.isCheckingSession, authState.isLoggedIn, currentDestination) {
        if (authState.isCheckingSession) return@LaunchedEffect

        val targetGraphRoute = if (authState.isLoggedIn) {
            Graph.Main.route
        } else {
            Graph.Auth.route
        }

        val alreadyOnTargetGraph = currentDestination
            ?.hierarchy
            ?.any { it.route == targetGraphRoute } == true

        if (!alreadyOnTargetGraph) {
            navController.navigate(targetGraphRoute) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}