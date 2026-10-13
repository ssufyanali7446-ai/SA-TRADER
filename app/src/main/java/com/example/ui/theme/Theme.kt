package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = SlateSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = BullishGreen,
    onTertiary = Color.White,
    background = SlateDarkBackground,
    onBackground = TextPrimary,
    surface = SlateDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = SlateDarkBorder,
    error = BearishRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Defaulting trading terminal to sleek dark aesthetics
    primary = GoldPrimary,
    onPrimary = Color.Black,
    secondary = CyanAccent,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to professional dark trading terminal
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
