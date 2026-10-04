package com.metamonjurul.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Glassmorphism Color Scheme - Dark theme with golden accents
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),       // Gold
    onPrimary = Color(0xFF1A1A2E),     // Dark navy
    primaryContainer = Color(0xFFFF8C00), // Dark orange/gold
    onPrimaryContainer = Color(0xFF1A1A2E),
    secondary = Color(0xFFFFD700),     // Gold
    onSecondary = Color(0xFF1A1A2E),
    secondaryContainer = Color(0xFFFFA500), // Orange
    onSecondaryContainer = Color(0xFF1A1A2E),
    tertiary = Color(0xFF00FFFF),      // Cyan accent
    onTertiary = Color(0xFF1A1A2E),
    tertiaryContainer = Color(0xFF00BFFF),
    onTertiaryContainer = Color(0xFF1A1A2E),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A1A2E),
    errorContainer = Color(0xFFFF6B6B),
    onErrorContainer = Color(0xFF1A1A2E),
    background = Color(0xFF0F0F1A),    // Deep dark
    onBackground = Color(0xFFE8E8F0),
    surface = Color(0xFF1A1A2E),       // Dark card
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF2A2A4A), // Slightly lighter card
    onSurfaceVariant = Color(0xFFB0B0C8),
    outline = Color(0xFF3A3A5A),
    outlineVariant = Color(0xFF2A2A4A),
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8E8F0),
    inverseOnSurface = Color(0xFF1A1A2E),
    inversePrimary = Color(0xFFB8860B)
)

@Composable
fun MetaMonjurulTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Glassmorphism color extensions
val Color.glassSurface: Color
    get() = this.copy(alpha = 0.15f)

val Color.glassSurfaceStrong: Color
    get() = this.copy(alpha = 0.25f)

val Color.glassBorder: Color
    get() = this.copy(alpha = 0.2f)

val Color.glassHighlight: Color
    get() = this.copy(alpha = 0.1f)

val Color.goldGradientStart: Color
    get() = Color(0xFFFFD700)

val Color.goldGradientEnd: Color
    get() = Color(0xFFFFA500)