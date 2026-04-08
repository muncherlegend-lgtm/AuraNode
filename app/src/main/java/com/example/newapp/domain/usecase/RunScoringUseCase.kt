package com.example.newapp.domain.usecase

import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.RunSummary
import javax.inject.Inject
import kotlin.math.roundToInt

data class AnswerResolution(
    val scoreDelta: Int,
    val nextStreak: Int,
    val timeBonusDelta: Int
)

class RunScoringUseCase @Inject constructor() {

    fun resolveAnswer(
        mode: QuizMode,
        isCorrect: Boolean,
        secondsLeft: Int,
        currentStreak: Int
    ): AnswerResolution {
        if (!isCorrect) {
            return AnswerResolution(
                scoreDelta = 0,
                nextStreak = 0,
                timeBonusDelta = 0
            )
        }

        val nextStreak = currentStreak + 1
        val baseScore = when (mode) {
            QuizMode.CLASSIC -> 10
            QuizMode.SPRINT -> 12
            QuizMode.LEGEND -> 18
        }
        val streakBonus = when (mode) {
            QuizMode.CLASSIC -> 0
            QuizMode.SPRINT -> nextStreak * 3
            QuizMode.LEGEND -> nextStreak * 4
        }
        val timeBonus = when (mode) {
            QuizMode.CLASSIC -> 0
            QuizMode.SPRINT -> secondsLeft.coerceAtLeast(0)
            QuizMode.LEGEND -> (secondsLeft * 2).coerceAtLeast(0)
        }

        return AnswerResolution(
            scoreDelta = baseScore + streakBonus + timeBonus,
            nextStreak = nextStreak,
            timeBonusDelta = timeBonus
        )
    }

    fun calculateMedal(runSummary: RunSummary): MedalTier {
        val perfect = runSummary.correctAnswers == runSummary.totalQuestions
        return when {
            perfect && runSummary.mode == QuizMode.LEGEND -> MedalTier.AURORA
            perfect && runSummary.mode == QuizMode.SPRINT -> MedalTier.GOLD
            perfect -> MedalTier.GOLD
            runSummary.accuracyRatio >= 0.8f -> MedalTier.SILVER
            runSummary.accuracyRatio >= 0.6f -> MedalTier.BRONZE
            else -> MedalTier.NONE
        }
    }

    fun computeAccuracy(correctAnswers: Int, totalQuestions: Int): Float {
        if (totalQuestions <= 0) return 0f
        return (correctAnswers.toFloat() / totalQuestions).coerceIn(0f, 1f)
    }

    fun accuracyPercent(runSummary: RunSummary): Int =
        (runSummary.accuracyRatio * 100).roundToInt()
}
