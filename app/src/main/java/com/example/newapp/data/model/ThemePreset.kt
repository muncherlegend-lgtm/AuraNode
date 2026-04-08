package com.example.newapp.data.model

data class ThemePreset(
    val id: String,
    val title: String,
    val description: String,
    val isDark: Boolean,
    val backgroundStyle: BackgroundArtworkStyle,
    val palette: ThemePalette
)
