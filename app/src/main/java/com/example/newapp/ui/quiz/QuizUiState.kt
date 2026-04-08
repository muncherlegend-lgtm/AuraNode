package com.example.newapp.ui.quiz

import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AiGenerationConfig
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackSummary
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset

data class QuizUiState(
    val isLoading: Boolean = true,
    val selectedDifficulty: Difficulty? = null,
    val selectedMode: QuizMode = QuizMode.CLASSIC,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val expectedMaxScore: Int = 0,
    val correctAnswersCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val timeBonus: Int = 0,
    val timeoutCount: Int = 0,
    val isQuizCompleted: Boolean = false,
    val selectedAnswerIndex: Int? = null,
    val revealedAnswerIndex: Int? = null,
    val isAnswerLocked: Boolean = false,
    val answerFeedbackType: AnswerFeedbackType? = null,
    val timerSecondsLeft: Int = QuizSettings.DEFAULT_TIMER_SECONDS,
    val answerInput: String = "",
    val submittedAnswerText: String? = null,
    val quizSettings: QuizSettings = QuizSettings(),
    val availableThemes: List<ThemePreset> = emptyList(),
    val selectedThemeId: String = QuizSettings.DEFAULT_THEME_ID,
    val quizPacks: List<QuizPackSummary> = emptyList(),
    val selectedPackId: String = QuizPack.OFFICIAL_ALTAI_PACK_ID,
    val importedDocumentPreview: ImportedDocument? = null,
    val isGeneratingPack: Boolean = false,
    val generationWarnings: List<String> = emptyList(),
    val generationErrorMessage: String? = null,
    val aiGenerationConfig: AiGenerationConfig = AiGenerationConfig(),
    val isAiConsentSheetVisible: Boolean = false,
    val atlasNodes: List<AtlasNode> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val playerProgress: PlayerProgress = PlayerProgress(),
    val latestRunSummary: RunSummary? = null,
    val runUnlockedNodeIds: Set<String> = emptySet(),
    val runEarnedAchievementIds: Set<String> = emptySet(),
    val legendUnlockedDifficulties: Set<Difficulty> = emptySet()
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)

    val selectedTheme: ThemePreset?
        get() = availableThemes.firstOrNull { it.id == selectedThemeId } ?: availableThemes.firstOrNull()

    val selectedPack: QuizPackSummary?
        get() = quizPacks.firstOrNull { it.id == selectedPackId }

    val totalQuestions: Int
        get() = questions.size

    val progress: Float
        get() = if (questions.isEmpty()) 0f else (currentQuestionIndex + 1f) / questions.size.toFloat()

    val maxScore: Int
        get() = expectedMaxScore.coerceAtLeast(questions.size * POINTS_PER_CORRECT)

    val accuracyRatio: Float
        get() = if (questions.isEmpty()) 0f else correctAnswersCount / questions.size.toFloat()

    val unlockedAtlasNodes: List<AtlasNode>
        get() = atlasNodes.filter { playerProgress.unlockedAtlasNodeIds.contains(it.id) || runUnlockedNodeIds.contains(it.id) }

    val earnedAchievements: List<Achievement>
        get() = achievements.filter { playerProgress.unlockedAchievementIds.contains(it.id) || runEarnedAchievementIds.contains(it.id) }

    val hallOfFameRuns: List<RunSummary>
        get() = playerProgress.bestRuns.take(5)

    val atlasCompletionRatio: Float
        get() = if (atlasNodes.isEmpty()) 0f else unlockedAtlasNodes.size / atlasNodes.size.toFloat()

    val isLegendAvailable: Boolean
        get() = selectedDifficulty != null && legendUnlockedDifficulties.contains(selectedDifficulty)

    val officialPack: QuizPackSummary?
        get() = quizPacks.firstOrNull { it.type == QuizPackType.OFFICIAL_ALTAI }

    val customPacks: List<QuizPackSummary>
        get() = quizPacks.filter { it.type == QuizPackType.CUSTOM_IMPORTED }

    val isOfficialPackSelected: Boolean
        get() = selectedPack?.type != QuizPackType.CUSTOM_IMPORTED

    val shouldShowOnboarding: Boolean
        get() = !quizSettings.hasCompletedOnboarding

    val cloudGenerationReady: Boolean
        get() = aiGenerationConfig.cloudGenerationEnabled && aiGenerationConfig.apiKey.isNotBlank()

    companion object {
        const val POINTS_PER_CORRECT = 10
    }
}
