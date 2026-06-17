package com.fitnessapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FitnessColorScheme = lightColorScheme(
    primary = Color(0xFF1F8A5B),
    onPrimary = Color.White,
    secondary = Color(0xFFE89B2D),
    onSecondary = Color(0xFF211100),
    tertiary = Color(0xFF2E6FAD),
    onTertiary = Color.White,
    background = Color(0xFFF7F8F4),
    onBackground = Color(0xFF17211B),
    surface = Color.White,
    onSurface = Color(0xFF17211B),
    surfaceVariant = Color(0xFFE4ECE5),
    onSurfaceVariant = Color(0xFF435247)
)

@Composable
fun FitnessAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FitnessColorScheme,
        typography = Typography(),
        content = content
    )
}
