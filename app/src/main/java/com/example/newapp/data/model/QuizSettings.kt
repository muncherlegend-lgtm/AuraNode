package com.example.newapp.data.model

data class QuizSettings(
    val timerSeconds: Int = DEFAULT_TIMER_SECONDS,
    val autoAdvanceDelayMs: Long = DEFAULT_AUTO_ADVANCE_DELAY_MS,
    val showTimer: Boolean = true,
    val allowOptionSelection: Boolean = true,
    val allowFreeTextAnswers: Boolean = true,
    val compactUi: Boolean = true,
    val motionEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val juryModeEnabled: Boolean = true,
    val demoResetOnLaunch: Boolean = false,
    val shuffleQuestions: Boolean = false,
    val shuffleOptions: Boolean = false,
    val questionsPerDifficulty: Int = DEFAULT_QUESTIONS_PER_DIFFICULTY,
    val defaultThemeId: String = DEFAULT_THEME_ID,
    val defaultPackId: String = QuizPack.OFFICIAL_ALTAI_PACK_ID,
    val defaultMode: QuizMode = QuizMode.CLASSIC,
    val answerMode: AnswerMode = AnswerMode.CLASSIC_OPTIONS,
    val homeContentPreference: HomeContentPreference = HomeContentPreference.OFFICIAL_FIRST,
    val hasCompletedOnboarding: Boolean = false
) {
    companion object {
        const val DEFAULT_TIMER_SECONDS = 18
        const val DEFAULT_AUTO_ADVANCE_DELAY_MS = 1_700L
        const val DEFAULT_QUESTIONS_PER_DIFFICULTY = 5
        const val DEFAULT_THEME_ID = "katun_dawn"
    }
}
