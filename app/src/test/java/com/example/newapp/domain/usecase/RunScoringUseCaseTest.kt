package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.RunSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunScoringUseCaseTest {

    private val useCase = RunScoringUseCase()

    @Test
    fun classicCorrectAnswerUsesFixedContestScore() {
        val resolution = useCase.resolveAnswer(
            mode = QuizMode.CLASSIC,
            isCorrect = true,
            secondsLeft = 19,
            currentStreak = 2
        )

        assertEquals(10, resolution.scoreDelta)
        assertEquals(3, resolution.nextStreak)
        assertEquals(0, resolution.timeBonusDelta)
    }

    @Test
    fun sprintIncorrectAnswerResetsStreak() {
        val resolution = useCase.resolveAnswer(
            mode = QuizMode.SPRINT,
            isCorrect = false,
            secondsLeft = 12,
            currentStreak = 4
        )

        assertEquals(0, resolution.scoreDelta)
        assertEquals(0, resolution.nextStreak)
        assertEquals(0, resolution.timeBonusDelta)
    }

    @Test
    fun legendCorrectAnswerAddsStreakAndTimeBonus() {
        val resolution = useCase.resolveAnswer(
            mode = QuizMode.LEGEND,
            isCorrect = true,
            secondsLeft = 7,
            currentStreak = 2
        )

        assertEquals(44, resolution.scoreDelta)
        assertEquals(3, resolution.nextStreak)
        assertEquals(14, resolution.timeBonusDelta)
    }

    @Test
    fun perfectLegendRunAwardsAuroraMedal() {
        val medal = useCase.calculateMedal(
            runSummary = runSummary(
                mode = QuizMode.LEGEND,
                correctAnswers = 3,
                totalQuestions = 3,
                accuracyRatio = 1f
            )
        )

        assertEquals(MedalTier.AURORA, medal)
    }

    @Test
    fun accuracyIsSafeForEmptyRuns() {
        val accuracy = useCase.computeAccuracy(correctAnswers = 0, totalQuestions = 0)

        assertTrue(accuracy == 0f)
    }

    private fun runSummary(
        mode: QuizMode,
        correctAnswers: Int,
        totalQuestions: Int,
        accuracyRatio: Float
    ): RunSummary = RunSummary(
        timestamp = 1L,
        difficulty = Difficulty.CADET,
        mode = mode,
        themeId = "katun",
        score = 42,
        maxScore = 60,
        correctAnswers = correctAnswers,
        totalQuestions = totalQuestions,
        accuracyRatio = accuracyRatio,
        currentStreak = 0,
        longestStreak = 3,
        timeBonus = 10,
        medalTier = MedalTier.NONE
    )
}
