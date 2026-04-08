package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AchievementRuleType
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.RunSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementEvaluatorUseCaseTest {

    private val useCase = AchievementEvaluatorUseCase()

    @Test
    fun awardsPerfectRunWhenAchievementIsStillLocked() {
        val earned = useCase(
            achievements = listOf(
                achievement(id = "perfect", ruleType = AchievementRuleType.PERFECT_RUN)
            ),
            progress = PlayerProgress(),
            runSummary = runSummary(
                mode = QuizMode.CLASSIC,
                correctAnswers = 5,
                totalQuestions = 5
            ),
            atlasNodeCount = 8,
            themeCount = 5,
            timeoutCount = 0
        )

        assertEquals(listOf("perfect"), earned)
    }

    @Test
    fun doesNotReawardAchievementAlreadyStoredInProgress() {
        val earned = useCase(
            achievements = listOf(
                achievement(id = "perfect", ruleType = AchievementRuleType.PERFECT_RUN)
            ),
            progress = PlayerProgress(unlockedAchievementIds = setOf("perfect")),
            runSummary = runSummary(
                mode = QuizMode.CLASSIC,
                correctAnswers = 5,
                totalQuestions = 5
            ),
            atlasNodeCount = 8,
            themeCount = 5,
            timeoutCount = 0
        )

        assertEquals(emptyList<String>(), earned)
    }

    @Test
    fun combinesProgressAndCurrentRunForAtlasAndThemeAchievements() {
        val earned = useCase(
            achievements = listOf(
                achievement(id = "atlas", ruleType = AchievementRuleType.ATLAS_NODES, threshold = 3),
                achievement(id = "themes", ruleType = AchievementRuleType.ALL_THEMES_TRIED, threshold = 3)
            ),
            progress = PlayerProgress(
                unlockedAtlasNodeIds = setOf("barnaul", "biysk"),
                discoveredThemeIds = setOf("katun", "golden")
            ),
            runSummary = runSummary(
                mode = QuizMode.SPRINT,
                unlockedNodeIds = listOf("tigirek"),
                themeId = "night"
            ),
            atlasNodeCount = 8,
            themeCount = 5,
            timeoutCount = 1
        )

        assertEquals(listOf("atlas", "themes"), earned)
    }

    private fun achievement(
        id: String,
        ruleType: AchievementRuleType,
        threshold: Int = 1
    ): Achievement = Achievement(
        id = id,
        title = id,
        description = "test",
        iconName = "star",
        ruleType = ruleType,
        threshold = threshold
    )

    private fun runSummary(
        mode: QuizMode,
        correctAnswers: Int = 3,
        totalQuestions: Int = 5,
        unlockedNodeIds: List<String> = emptyList(),
        themeId: String = "katun"
    ): RunSummary = RunSummary(
        timestamp = 1L,
        difficulty = Difficulty.ENGINEER,
        mode = mode,
        themeId = themeId,
        score = 50,
        maxScore = 70,
        correctAnswers = correctAnswers,
        totalQuestions = totalQuestions,
        accuracyRatio = if (totalQuestions == 0) 0f else correctAnswers.toFloat() / totalQuestions,
        currentStreak = 0,
        longestStreak = 4,
        timeBonus = 8,
        medalTier = MedalTier.SILVER,
        unlockedNodeIds = unlockedNodeIds
    )
}
