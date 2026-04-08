package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.RunSummary
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendEligibilityUseCaseTest {

    private val useCase = LegendEligibilityUseCase()

    @Test
    fun unlocksLegendWhenDifficultyHasSilverOrBetterNonLegendRun() {
        val progress = PlayerProgress(
            bestRuns = listOf(
                runSummary(
                    difficulty = Difficulty.COSMONAUT,
                    mode = QuizMode.SPRINT,
                    medalTier = MedalTier.SILVER
                )
            )
        )

        assertTrue(useCase(progress, Difficulty.COSMONAUT))
    }

    @Test
    fun keepsLegendLockedForBronzeOrDifferentDifficulty() {
        val progress = PlayerProgress(
            bestRuns = listOf(
                runSummary(
                    difficulty = Difficulty.CADET,
                    mode = QuizMode.CLASSIC,
                    medalTier = MedalTier.GOLD
                ),
                runSummary(
                    difficulty = Difficulty.ENGINEER,
                    mode = QuizMode.SPRINT,
                    medalTier = MedalTier.BRONZE
                )
            )
        )

        assertFalse(useCase(progress, Difficulty.ENGINEER))
        assertFalse(useCase(progress, Difficulty.COSMONAUT))
    }

    private fun runSummary(
        difficulty: Difficulty,
        mode: QuizMode,
        medalTier: MedalTier
    ): RunSummary = RunSummary(
        timestamp = 1L,
        difficulty = difficulty,
        mode = mode,
        themeId = "katun",
        score = 60,
        maxScore = 80,
        correctAnswers = 4,
        totalQuestions = 5,
        accuracyRatio = 0.8f,
        currentStreak = 0,
        longestStreak = 4,
        timeBonus = 10,
        medalTier = medalTier
    )
}
