package com.example.newapp.data.model

data class PlayerProgress(
    val unlockedAtlasNodeIds: Set<String> = emptySet(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val discoveredThemeIds: Set<String> = emptySet(),
    val bestRuns: List<RunSummary> = emptyList(),
    val latestRun: RunSummary? = null
) {
    fun hasAchievement(achievementId: String): Boolean = unlockedAchievementIds.contains(achievementId)

    fun hasAtlasNode(nodeId: String): Boolean = unlockedAtlasNodeIds.contains(nodeId)
}
