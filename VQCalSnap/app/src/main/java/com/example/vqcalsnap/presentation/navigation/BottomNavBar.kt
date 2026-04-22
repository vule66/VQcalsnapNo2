package com.example.vqcalsnap.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

data class NavItem(val label: String, val icon: ImageVector, val screen: Screen)

@Composable
fun BottomNavBar(navController: NavController, current: Screen) {
    val items = listOf(
        NavItem("Profile", Icons.Default.Person, Screen.Profile),
        NavItem("Camera", Icons.Default.AddCircle, Screen.Camera),
        NavItem("Lịch sử", Icons.Default.DateRange, Screen.History),
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.screen,
                onClick = {
                    if (current != item.screen) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Graph.Main.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}