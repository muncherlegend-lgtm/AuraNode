package com.example.newapp.data.model

data class RunSummary(
    val timestamp: Long,
    val packId: String = QuizPack.OFFICIAL_ALTAI_PACK_ID,
    val packTitle: String = "",
    val packType: QuizPackType = QuizPackType.OFFICIAL_ALTAI,
    val packGenerationSource: PackGenerationSource = PackGenerationSource.OFFICIAL,
    val sourceFileName: String = "",
    val difficulty: Difficulty,
    val mode: QuizMode,
    val themeId: String,
    val score: Int,
    val maxScore: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val accuracyRatio: Float,
    val currentStreak: Int,
    val longestStreak: Int,
    val timeBonus: Int,
    val medalTier: MedalTier,
    val unlockedNodeIds: List<String> = emptyList(),
    val earnedAchievementIds: List<String> = emptyList()
)
