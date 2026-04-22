package com.example.vqcalsnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun VQCalSnapTheme(
    activeGoal: String = "Duy trì cân nặng",
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        goalDarkColorScheme(activeGoal)
    } else {
        goalLightColorScheme(activeGoal)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun goalLightColorScheme(goal: String) = when (goal) {
    "Giảm cân" -> lightColorScheme(
        primary = LosePrimaryLight,
        onPrimary = LoseOnPrimaryLight,
        primaryContainer = LosePrimaryContainerLight,
        onPrimaryContainer = LoseOnPrimaryContainerLight,
        secondary = LoseSecondaryLight,
        tertiary = LoseTertiaryLight
    )

    "Tăng cân" -> lightColorScheme(
        primary = GainPrimaryLight,
        onPrimary = GainOnPrimaryLight,
        primaryContainer = GainPrimaryContainerLight,
        onPrimaryContainer = GainOnPrimaryContainerLight,
        secondary = GainSecondaryLight,
        tertiary = GainTertiaryLight
    )

    else -> lightColorScheme(
        primary = MaintainPrimaryLight,
        onPrimary = MaintainOnPrimaryLight,
        primaryContainer = MaintainPrimaryContainerLight,
        onPrimaryContainer = MaintainOnPrimaryContainerLight,
        secondary = MaintainSecondaryLight,
        tertiary = MaintainTertiaryLight
    )
}

private fun goalDarkColorScheme(goal: String) = when (goal) {
    "Giảm cân" -> darkColorScheme(
        primary = LosePrimaryDark,
        onPrimary = LoseOnPrimaryDark,
        primaryContainer = LosePrimaryContainerDark,
        onPrimaryContainer = LoseOnPrimaryContainerDark,
        secondary = LoseSecondaryDark,
        tertiary = LoseTertiaryDark
    )

    "Tăng cân" -> darkColorScheme(
        primary = GainPrimaryDark,
        onPrimary = GainOnPrimaryDark,
        primaryContainer = GainPrimaryContainerDark,
        onPrimaryContainer = GainOnPrimaryContainerDark,
        secondary = GainSecondaryDark,
        tertiary = GainTertiaryDark
    )

    else -> darkColorScheme(
        primary = MaintainPrimaryDark,
        onPrimary = MaintainOnPrimaryDark,
        primaryContainer = MaintainPrimaryContainerDark,
        onPrimaryContainer = MaintainOnPrimaryContainerDark,
        secondary = MaintainSecondaryDark,
        tertiary = MaintainTertiaryDark
    )
}

