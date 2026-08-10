package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SleekPrimaryDark,
    onPrimary = Color(0xFF002E69),
    primaryContainer = SleekPrimaryContainerDark,
    onPrimaryContainer = SleekOnPrimaryContainerDark,
    secondary = SleekSecondaryDark,
    onSecondary = Color(0xFF003737),
    secondaryContainer = SleekSecondaryContainerDark,
    onSecondaryContainer = SleekOnSecondaryContainerDark,
    tertiary = SleekTertiaryDark,
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = SleekTertiaryContainerDark,
    onTertiaryContainer = SleekOnTertiaryContainerDark,
    background = SleekBackgroundDark,
    onBackground = SleekTextPrimaryDark,
    surface = SleekSurfaceDark,
    onSurface = SleekTextPrimaryDark,
    surfaceVariant = SleekSurfaceVariantDark,
    onSurfaceVariant = SleekTextSecondaryDark,
    outline = SleekOutlineDark,
    outlineVariant = SleekOutlineVariantDark,
    error = StatusOverdueRed,
    errorContainer = Color(0xFF93000A),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryContainerLight,
    onPrimaryContainer = SleekOnPrimaryContainerLight,
    secondary = SleekSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SleekSecondaryContainerLight,
    onSecondaryContainer = SleekOnSecondaryContainerLight,
    tertiary = SleekTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SleekTertiaryContainerLight,
    onTertiaryContainer = SleekOnTertiaryContainerLight,
    background = SleekBackgroundLight,
    onBackground = SleekTextPrimaryLight,
    surface = SleekSurfaceLight,
    onSurface = SleekTextPrimaryLight,
    surfaceVariant = SleekSurfaceVariantLight,
    onSurfaceVariant = SleekTextSecondaryLight,
    outline = SleekOutlineLight,
    outlineVariant = SleekOutlineVariantLight,
    error = StatusOverdueRed,
    errorContainer = StatusOverdueRedContainer,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Clean, crisp, high-contrast light theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

