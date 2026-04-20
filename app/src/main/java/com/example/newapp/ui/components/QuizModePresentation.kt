package com.example.newapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.newapp.data.model.QuizMode
import com.example.newapp.ui.copy.longDescription
import com.example.newapp.ui.copy.modeHighlights
import com.example.newapp.ui.copy.uiLabel
import com.example.newapp.ui.theme.AltaiGold
import com.example.newapp.ui.theme.KatunTurquoise
import com.example.newapp.ui.theme.PineShadow

data class QuizModePresentation(
    val title: String,
    val body: String,
    val highlights: List<String>,
    val icon: ImageVector,
    val accent: Color
)

fun QuizMode.toPresentation(): QuizModePresentation = when (this) {
    QuizMode.CLASSIC -> QuizModePresentation(
        title = uiLabel(),
        body = longDescription(),
        highlights = modeHighlights(),
        icon = Icons.Filled.Explore,
        accent = KatunTurquoise
    )

    QuizMode.SPRINT -> QuizModePresentation(
        title = uiLabel(),
        body = longDescription(),
        highlights = modeHighlights(),
        icon = Icons.Filled.Bolt,
        accent = AltaiGold
    )

    QuizMode.LEGEND -> QuizModePresentation(
        title = uiLabel(),
        body = longDescription(),
        highlights = modeHighlights(),
        icon = Icons.Filled.AutoAwesome,
        accent = PineShadow
    )
}
