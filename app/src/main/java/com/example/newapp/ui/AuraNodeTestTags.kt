package com.example.newapp.ui

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode

object AuraNodeTestTags {
    const val MENU_SCREEN = "menu_screen"
    const val QUIZ_SCREEN = "quiz_screen"
    const val RESULT_SCREEN = "result_screen"
    const val ATLAS_SCREEN = "atlas_screen"
    const val SETTINGS_SCREEN = "settings_screen"
    const val THEMES_SCREEN = "themes_screen"
    const val MATERIALS_SCREEN = "materials_screen"
    const val ATLAS_FOCUS_LATEST = "atlas_focus_latest"
    const val ATLAS_PANEL_DETAILS = "atlas_panel_details"
    const val ATLAS_PANEL_PROGRESS = "atlas_panel_progress"
    const val MENU_OPEN_SETTINGS = "menu_open_settings"
    const val MENU_OPEN_THEMES = "menu_open_themes"
    const val MENU_OPEN_ATLAS = "menu_open_atlas"
    const val MENU_IMPORT_MATERIAL = "menu_import_material"
    const val RESULT_RETRY = "result_retry"
    const val RESULT_OPEN_ATLAS = "result_open_atlas"

    fun difficultyTag(difficulty: Difficulty): String = "difficulty_${difficulty.name.lowercase()}"

    fun answerOptionTag(index: Int): String = "answer_option_$index"

    fun themeTag(themeId: String): String = "theme_$themeId"

    fun modeTag(mode: QuizMode): String = "mode_${mode.name.lowercase()}"

    fun packTag(packId: String): String = "pack_$packId"
}
