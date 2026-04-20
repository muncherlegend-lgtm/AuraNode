package com.example.newapp.ui.share

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import com.example.newapp.ui.copy.APP_NAME
import kotlin.math.roundToInt

data class ResultShareContent(
    val packCategory: String,
    val packTitle: String,
    val verdictLabel: String,
    val scoreLabel: String,
    val accuracyLabel: String,
    val correctAnswersLabel: String,
    val streakLabel: String,
    val modeLabel: String,
    val difficultyLabel: String,
    val highlightFact: String,
    val footerLabel: String
)

fun buildResultShareContent(
    runSummary: RunSummary,
    themePreset: ThemePreset?,
    highlightFact: String
): ResultShareContent {
    val accuracyPercent = (runSummary.accuracyRatio * 100).roundToInt()
    return ResultShareContent(
        packCategory = when (runSummary.packType) {
            QuizPackType.OFFICIAL_ALTAI -> "Алтайский маршрут"
            QuizPackType.CUSTOM_IMPORTED -> "Пользовательский набор"
        },
        packTitle = runSummary.packTitle.ifBlank {
            when (runSummary.packType) {
                QuizPackType.OFFICIAL_ALTAI -> "Маршрут по Алтайскому краю"
                QuizPackType.CUSTOM_IMPORTED -> "Новый пользовательский набор"
            }
        },
        verdictLabel = runSummary.medalTier.shareVerdictLabel(accuracyPercent),
        scoreLabel = "${runSummary.score} / ${runSummary.maxScore}",
        accuracyLabel = "Точность $accuracyPercent%",
        correctAnswersLabel = "${runSummary.correctAnswers} из ${runSummary.totalQuestions}",
        streakLabel = "Серия ${runSummary.longestStreak}",
        modeLabel = runSummary.mode.shareLabel(),
        difficultyLabel = runSummary.difficulty.shareLabel(),
        highlightFact = highlightFact.trim().ifBlank { "Результат сохранён локально." },
        footerLabel = when {
            runSummary.sourceFileName.isNotBlank() -> "Материал: ${runSummary.sourceFileName}"
            themePreset != null -> "Тема: ${themePreset.title}"
            else -> "Локальный результат «$APP_NAME»"
        }
    )
}

fun ResultShareContent.toShareText(): String = buildString {
    appendLine(APP_NAME)
    appendLine(packCategory)
    appendLine(packTitle)
    appendLine("Счёт: $scoreLabel")
    appendLine(accuracyLabel)
    appendLine("Верно: $correctAnswersLabel")
    appendLine("Режим: $modeLabel")
    appendLine("Уровень: $difficultyLabel")
    appendLine("Итог: $verdictLabel")
    appendLine(footerLabel)
    appendLine()
    append(highlightFact)
}

fun QuizMode.shareLabel(): String = when (this) {
    QuizMode.CLASSIC -> "Основной"
    QuizMode.SPRINT -> "Быстрый"
    QuizMode.LEGEND -> "Углубленный"
}

fun Difficulty.shareLabel(): String = when (this) {
    Difficulty.CADET -> "Кадет"
    Difficulty.ENGINEER -> "Инженер"
    Difficulty.COSMONAUT -> "Космонавт"
}

private fun MedalTier.shareVerdictLabel(accuracyPercent: Int): String = when (this) {
    MedalTier.AURORA -> "Лучший результат"
    MedalTier.GOLD -> "Безошибочное прохождение"
    MedalTier.SILVER -> "Очень сильный результат"
    MedalTier.BRONZE -> "Уверенный результат"
    MedalTier.NONE -> if (accuracyPercent >= 70) "Маршрут пройден" else "Маршрут завершён"
}
