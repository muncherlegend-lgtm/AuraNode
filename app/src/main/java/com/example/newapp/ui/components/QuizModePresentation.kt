package com.example.newapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.newapp.R
import com.example.newapp.data.model.QuizMode
import com.example.newapp.ui.theme.AltaiGold
import com.example.newapp.ui.theme.KatunTurquoise
import com.example.newapp.ui.theme.PineShadow

data class QuizModePresentation(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
    val icon: ImageVector,
    val accent: Color
)

fun QuizMode.toPresentation(): QuizModePresentation = when (this) {
    QuizMode.CLASSIC -> QuizModePresentation(
        titleRes = R.string.mode_classic_title,
        bodyRes = R.string.mode_classic_body,
        icon = Icons.Filled.Explore,
        accent = KatunTurquoise
    )

    QuizMode.SPRINT -> QuizModePresentation(
        titleRes = R.string.mode_sprint_title,
        bodyRes = R.string.mode_sprint_body,
        icon = Icons.Filled.Bolt,
        accent = AltaiGold
    )

    QuizMode.LEGEND -> QuizModePresentation(
        titleRes = R.string.mode_legend_title,
        bodyRes = R.string.mode_legend_body,
        icon = Icons.Filled.AutoAwesome,
        accent = PineShadow
    )
}
