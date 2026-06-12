package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = OceanLight.copy(alpha = 0.3f),
    onPrimaryContainer = OceanDark,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealLight.copy(alpha = 0.3f),
    onSecondaryContainer = Color(0xFF0B3D35),
    tertiary = WarmAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarmAmber.copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF3E2A0A),
    error = SoftCoral,
    background = Color(0xFFF5FAFE),
    onBackground = Color(0xFF0D1B2A),
    surface = Color.White,
    onSurface = Color(0xFF0D1B2A),
    surfaceVariant = Color(0xFFE8F4F8),
    onSurfaceVariant = Color(0xFF2D3F4E),
    outline = Color(0xFFB0C4D8)
)

private val DarkColorScheme = darkColorScheme(
    primary = OceanLight,
    onPrimary = OceanDark,
    primaryContainer = OceanDark,
    onPrimaryContainer = OceanLight.copy(alpha = 0.8f),
    secondary = TealLight,
    onSecondary = Color(0xFF0B3D35),
    secondaryContainer = Color(0xFF145A4E),
    onSecondaryContainer = TealLight.copy(alpha = 0.8f),
    tertiary = WarmAmber,
    onTertiary = Color(0xFF3E2A0A),
    tertiaryContainer = Color(0xFF5C3E10),
    onTertiaryContainer = WarmAmber.copy(alpha = 0.8f),
    error = Color(0xFFFF6B6B),
    background = DarkBackground,
    onBackground = Color(0xFFE1ECF5),
    surface = DarkSurface,
    onSurface = Color(0xFFE1ECF5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0C4D8),
    outline = Color(0xFF4A5D6E)
)

@Composable
fun FishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
