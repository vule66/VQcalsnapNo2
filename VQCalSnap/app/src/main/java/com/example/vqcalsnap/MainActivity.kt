package com.example.vqcalsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.vqcalsnap.presentation.navigation.NavGraph
import com.example.vqcalsnap.presentation.theme.ThemeViewModel
import com.example.vqcalsnap.ui.theme.VQCalSnapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val activeGoal = themeViewModel.activeGoal.collectAsStateWithLifecycle()

            VQCalSnapTheme(
                activeGoal = activeGoal.value,
                darkTheme = false
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}