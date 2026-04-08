package com.example.newapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.newapp.R
import com.example.newapp.data.model.Difficulty
import com.example.newapp.ui.theme.AltaiAmber
import com.example.newapp.ui.theme.AltaiGold
import com.example.newapp.ui.theme.AltaiSand
import com.example.newapp.ui.theme.KatunDeep
import com.example.newapp.ui.theme.KatunTurquoise
import com.example.newapp.ui.theme.Moonlight
import com.example.newapp.ui.theme.PineShadow

data class DifficultyPresentation(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val badgeRes: Int,
    val icon: ImageVector,
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

fun Difficulty.toPresentation(): DifficultyPresentation {
    return when (this) {
        Difficulty.CADET -> DifficultyPresentation(
            titleRes = R.string.difficulty_cadet_title,
            subtitleRes = R.string.difficulty_cadet_subtitle,
            badgeRes = R.string.difficulty_cadet_badge,
            icon = Icons.Filled.School,
            accent = KatunTurquoise,
            container = Color(0xFFE1F3F1),
            onContainer = KatunDeep
        )

        Difficulty.ENGINEER -> DifficultyPresentation(
            titleRes = R.string.difficulty_engineer_title,
            subtitleRes = R.string.difficulty_engineer_subtitle,
            badgeRes = R.string.difficulty_engineer_badge,
            icon = Icons.Filled.Engineering,
            accent = AltaiGold,
            container = AltaiSand,
            onContainer = AltaiAmber
        )

        Difficulty.COSMONAUT -> DifficultyPresentation(
            titleRes = R.string.difficulty_cosmonaut_title,
            subtitleRes = R.string.difficulty_cosmonaut_subtitle,
            badgeRes = R.string.difficulty_cosmonaut_badge,
            icon = Icons.Filled.RocketLaunch,
            accent = PineShadow,
            container = KatunDeep,
            onContainer = Moonlight
        )
    }
}
