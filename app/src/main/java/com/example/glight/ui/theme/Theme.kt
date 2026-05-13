package com.example.glight.ui.theme

import android.app.Activity
import android.os.Build
import android.view.Window
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
    primary = PrimaryLightBlue,
    secondary = PrimaryBlue,
    tertiary = PrimaryLightBlue,
    background = NightBackground,
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1E293B),
    onPrimary = NightBackground,
    onSecondary = BackgroundLight,
    onTertiary = NightBackground,
    onBackground = BackgroundLight,
    onSurface = BackgroundLight,
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onError = Color(0xFF601410),
    outline = Color(0xFF938F99),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryLightBlue,
    tertiary = PrimaryBlue,
    background = BackgroundLight,
    surface = CardLight,
    surfaceVariant = SurfaceVariant,
    onPrimary = BackgroundLight,
    onSecondary = TextPrimary,
    onTertiary = BackgroundLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
    errorContainer = ErrorContainer,
    onError = BackgroundLight,
    outline = Outline,
)

@Composable
fun GlightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            setStatusBarColor(window, colorScheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Suppress("DEPRECATION")
private fun setStatusBarColor(window: Window, color: Int) {
    window.statusBarColor = color
}
