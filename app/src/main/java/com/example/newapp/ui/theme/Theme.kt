package com.example.newapp.ui.theme

import android.app.Activity
import android.graphics.Color.parseColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.newapp.data.model.ThemePreset

private val DarkColorScheme = darkColorScheme(
    primary = KatunTurquoise,
    onPrimary = Twilight,
    secondary = AltaiGold,
    onSecondary = Twilight,
    tertiary = AltaiSage,
    onTertiary = Moonlight,
    background = KatunNight,
    onBackground = Moonlight,
    surface = PineShadow,
    onSurface = Moonlight,
    surfaceVariant = Color(0xFF213D42),
    onSurfaceVariant = Color(0xFFD5E6E4),
    primaryContainer = Color(0xFF0E3C42),
    onPrimaryContainer = Color(0xFFA8E4DF),
    secondaryContainer = Color(0xFF3D2A07),
    onSecondaryContainer = Color(0xFFFFE8BA),
    tertiaryContainer = Color(0xFF203A31),
    onTertiaryContainer = Color(0xFFD8E9E0)
)

private val LightColorScheme = lightColorScheme(
    primary = KatunDeep,
    onPrimary = Color.White,
    secondary = AltaiGold,
    onSecondary = Color(0xFF241A00),
    tertiary = AltaiSage,
    onTertiary = Color.White,
    background = Color(0xFFF6F1E7),
    onBackground = Granite,
    surface = SurfaceWarm,
    onSurface = Granite,
    surfaceVariant = RiverMist,
    onSurfaceVariant = Color(0xFF43595B),
    secondaryContainer = Color(0xFFFFE7B4),
    onSecondaryContainer = Color(0xFF3A2A00),
    tertiaryContainer = Color(0xFFD6E8D8),
    onTertiaryContainer = Color(0xFF15301C),
    primaryContainer = Color(0xFFC8ECE8),
    onPrimaryContainer = Color(0xFF022F35),
    outline = Color(0x4D6B7F80)
)

@Composable
fun AuraNodeTheme(
    themePreset: ThemePreset? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val useDarkTheme = themePreset?.isDark ?: darkTheme
    val colorScheme = when {
        themePreset != null -> themePreset.toColorScheme()
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !useDarkTheme
            insetsController.isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AuraNodeShapes,
        content = content
    )
}

private fun ThemePreset.toColorScheme() = if (isDark) {
    darkColorScheme(
        primary = palette.primary.toComposeColor(),
        onPrimary = palette.onPrimary.toComposeColor(),
        secondary = palette.secondary.toComposeColor(),
        onSecondary = palette.onSecondary.toComposeColor(),
        tertiary = palette.tertiary.toComposeColor(),
        onTertiary = palette.onTertiary.toComposeColor(),
        background = palette.background.toComposeColor(),
        onBackground = palette.onBackground.toComposeColor(),
        surface = palette.surface.toComposeColor(),
        onSurface = palette.onSurface.toComposeColor(),
        surfaceVariant = palette.surfaceVariant.toComposeColor(),
        onSurfaceVariant = palette.onSurfaceVariant.toComposeColor(),
        primaryContainer = palette.primaryContainer.toComposeColor(),
        onPrimaryContainer = palette.onPrimaryContainer.toComposeColor(),
        secondaryContainer = palette.secondaryContainer.toComposeColor(),
        onSecondaryContainer = palette.onSecondaryContainer.toComposeColor(),
        tertiaryContainer = palette.tertiaryContainer.toComposeColor(),
        onTertiaryContainer = palette.onTertiaryContainer.toComposeColor(),
        outline = palette.outline.toComposeColor()
    )
} else {
    lightColorScheme(
        primary = palette.primary.toComposeColor(),
        onPrimary = palette.onPrimary.toComposeColor(),
        secondary = palette.secondary.toComposeColor(),
        onSecondary = palette.onSecondary.toComposeColor(),
        tertiary = palette.tertiary.toComposeColor(),
        onTertiary = palette.onTertiary.toComposeColor(),
        background = palette.background.toComposeColor(),
        onBackground = palette.onBackground.toComposeColor(),
        surface = palette.surface.toComposeColor(),
        onSurface = palette.onSurface.toComposeColor(),
        surfaceVariant = palette.surfaceVariant.toComposeColor(),
        onSurfaceVariant = palette.onSurfaceVariant.toComposeColor(),
        primaryContainer = palette.primaryContainer.toComposeColor(),
        onPrimaryContainer = palette.onPrimaryContainer.toComposeColor(),
        secondaryContainer = palette.secondaryContainer.toComposeColor(),
        onSecondaryContainer = palette.onSecondaryContainer.toComposeColor(),
        tertiaryContainer = palette.tertiaryContainer.toComposeColor(),
        onTertiaryContainer = palette.onTertiaryContainer.toComposeColor(),
        outline = palette.outline.toComposeColor()
    )
}

private fun String.toComposeColor(): Color = Color(parseColor(this))
