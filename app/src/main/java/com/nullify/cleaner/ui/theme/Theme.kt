package com.nullify.cleaner.ui.theme

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

private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)
private val Purple40 = Color(0xFF6650A4)
private val PurpleGrey40 = Color(0xFF625B71)
private val Pink40 = Color(0xFF7D5260)

private val AmoledDark = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40
)

@Composable
fun MyComposeApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledBlack: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        amoledBlack && darkTheme -> AmoledDark
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
            @Suppress("DEPRECATION")
            window.statusBarColor = Color(0xFF000000).toArgb()
            window.navigationBarColor = Color(0xFF000000).toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
