package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = StudioSurfaceHighlight,
    onPrimaryContainer = StudioCyan,
    secondary = StudioAmber,
    onSecondary = Color(0xFF261A00),
    secondaryContainer = StudioSurfaceElevated,
    onSecondaryContainer = StudioAmber,
    tertiary = StudioEmerald,
    onTertiary = Color(0xFF00220E),
    background = StudioBackground,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioBorderLight,
    error = StudioRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to professional dark cinematic mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
