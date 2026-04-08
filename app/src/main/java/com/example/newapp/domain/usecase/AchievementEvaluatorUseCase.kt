package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AchievementRuleType
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.RunSummary
import javax.inject.Inject

class AchievementEvaluatorUseCase @Inject constructor() {

    operator fun invoke(
        achievements: List<Achievement>,
        progress: PlayerProgress,
        runSummary: RunSummary,
        atlasNodeCount: Int,
        themeCount: Int,
        timeoutCount: Int
    ): List<String> {
        return achievements.mapNotNull { achievement ->
            if (progress.hasAchievement(achievement.id)) {
                null
            } else if (matchesRule(achievement, progress, runSummary, atlasNodeCount, themeCount, timeoutCount)) {
                achievement.id
            } else {
                null
            }
        }
    }

    private fun matchesRule(
        achievement: Achievement,
        progress: PlayerProgress,
        runSummary: RunSummary,
        atlasNodeCount: Int,
        themeCount: Int,
        timeoutCount: Int
    ): Boolean = when (achievement.ruleType) {
        AchievementRuleType.PERFECT_RUN -> runSummary.correctAnswers == runSummary.totalQuestions
        AchievementRuleType.NO_TIMEOUTS -> timeoutCount == 0
        AchievementRuleType.STREAK -> runSummary.longestStreak >= achievement.threshold
        AchievementRuleType.COMPLETE_MODE -> progress.bestRuns.any { it.mode == runSummary.mode }
        AchievementRuleType.ATLAS_NODES -> {
            (progress.unlockedAtlasNodeIds + runSummary.unlockedNodeIds.toSet()).size >= achievement.threshold &&
                atlasNodeCount >= achievement.threshold
        }

        AchievementRuleType.ALL_THEMES_TRIED -> {
            (progress.discoveredThemeIds + runSummary.themeId).size >= achievement.threshold &&
                themeCount >= achievement.threshold
        }

        AchievementRuleType.LEGEND_CLEAR -> runSummary.mode == QuizMode.LEGEND && runSummary.correctAnswers > 0
    }
}
