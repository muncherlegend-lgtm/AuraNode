package com.example.newapp.ui

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode

object AuraNodeTestTags {
    const val MENU_SCREEN = "menu_screen"
    const val QUIZ_SCREEN = "quiz_screen"
    const val RESULT_SCREEN = "result_screen"
    const val ATLAS_SCREEN = "atlas_screen"
    const val ATLAS_FOCUS_LATEST = "atlas_focus_latest"
    const val ATLAS_PANEL_DETAILS = "atlas_panel_details"
    const val ATLAS_PANEL_PROGRESS = "atlas_panel_progress"
    const val MENU_ONBOARDING = "menu_onboarding"
    const val MENU_ONBOARDING_CONTINUE = "menu_onboarding_continue"
    const val MENU_OPEN_SETTINGS = "menu_open_settings"
    const val MENU_OPEN_ATLAS = "menu_open_atlas"
    const val MENU_IMPORT_MATERIAL = "menu_import_material"
    const val MENU_RESET_PROGRESS = "menu_reset_progress"
    const val MENU_GENERATE_CLOUD = "menu_generate_cloud"
    const val MENU_GENERATE_OFFLINE = "menu_generate_offline"
    const val QUIZ_TEXT_INPUT = "quiz_text_input"
    const val QUIZ_TEXT_SUBMIT = "quiz_text_submit"
    const val RESULT_RETRY = "result_retry"
    const val RESULT_OPEN_ATLAS = "result_open_atlas"

    fun difficultyTag(difficulty: Difficulty): String = "difficulty_${difficulty.name.lowercase()}"

    fun answerOptionTag(index: Int): String = "answer_option_$index"

    fun themeTag(themeId: String): String = "theme_$themeId"

    fun modeTag(mode: QuizMode): String = "mode_${mode.name.lowercase()}"

    fun packTag(packId: String): String = "pack_$packId"
}
