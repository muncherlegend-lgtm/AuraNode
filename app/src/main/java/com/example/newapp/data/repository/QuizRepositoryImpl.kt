package com.example.newapp.data.repository

import android.net.Uri
import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AiGenerationConfig
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.CloudGenerationMode
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.PackGenerationResult
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackSummary
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import com.example.newapp.data.source.CloudQuizPackGenerator
import com.example.newapp.data.source.DocumentImportDataSource
import com.example.newapp.data.source.GeneratedPackMerger
import com.example.newapp.data.source.LocalQuizDataSource
import com.example.newapp.data.source.QuizConfigParser
import com.example.newapp.data.source.QuizPackStorageDataSource
import com.example.newapp.data.source.QuizPreferencesDataSource
import com.example.newapp.domain.usecase.OfflineQuizPackGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val localQuizDataSource: LocalQuizDataSource,
    private val quizPreferencesDataSource: QuizPreferencesDataSource,
    private val quizPackStorageDataSource: QuizPackStorageDataSource,
    private val documentImportDataSource: DocumentImportDataSource,
    private val cloudQuizPackGenerator: CloudQuizPackGenerator,
    private val offlineQuizPackGenerator: OfflineQuizPackGenerator,
    private val generatedPackMerger: GeneratedPackMerger
) : QuizRepository {

    private val packCache = linkedMapOf<String, QuizPack>()

    override fun getQuestionsByDifficulty(
        difficulty: Difficulty,
        mode: QuizMode,
        packId: String
    ): List<Question> {
        val pack = resolvePack(packId) ?: return emptyList()
        val safeMode = if (pack.type == com.example.newapp.data.model.QuizPackType.CUSTOM_IMPORTED) {
            QuizMode.CLASSIC
        } else {
            mode
        }
        val questions = pack.questions
            .filter { it.difficulty == difficulty }
            .sortedBy { it.id }

        return when (safeMode) {
            QuizMode.CLASSIC -> questions
            QuizMode.SPRINT -> questions.sortedByDescending { it.difficultyWeight }
            QuizMode.LEGEND -> {
                val legendary = questions.filter { it.isLegendary }
                val remaining = questions.filterNot { it.isLegendary }.sortedByDescending { it.difficultyWeight }
                (legendary + remaining).distinctBy { it.id }.take(3)
            }
        }
    }

    override fun getQuizConfig(): QuizSettings = localQuizDataSource.getQuizSettings()

    override suspend fun getQuizSettings(): QuizSettings =
        quizPreferencesDataSource.readMergedSettings(getQuizConfig())

    override fun getThemePresets(): List<ThemePreset> = localQuizDataSource.getThemePresets()

    override fun getAtlasNodes(): List<AtlasNode> = localQuizDataSource.getAtlasNodes()

    override fun getAchievements(): List<Achievement> = localQuizDataSource.getAchievements()

    override suspend fun getQuizPacks(): List<QuizPackSummary> {
        val officialPack = officialPack()
        val customPacks = quizPackStorageDataSource.loadAllCustomPacks()
        packCache.clear()
        packCache[officialPack.id] = officialPack
        customPacks.forEach { packCache[it.id] = it }
        return buildList {
            add(officialPack.toSummary())
            addAll(customPacks.map { it.toSummary() })
        }
    }

    override suspend fun getQuizPack(packId: String): QuizPack? {
        if (packId == QuizPack.OFFICIAL_ALTAI_PACK_ID) {
            return officialPack()
        }
        packCache[packId]?.let { return it }
        return quizPackStorageDataSource.loadCustomPack(packId)?.also { packCache[it.id] = it }
    }

    override suspend fun prepareImportedDocument(uri: Uri): ImportedDocument =
        documentImportDataSource.prepareImportedDocument(uri)

    override suspend fun generateImportedPack(
        document: ImportedDocument,
        mode: CloudGenerationMode
    ): PackGenerationResult {
        val settings = getQuizSettings()
        val offlinePack = offlineQuizPackGenerator.generate(document, settings.questionsPerDifficulty)
        val aiConfig = quizPreferencesDataSource.readAiGenerationConfig()
        val cloudResult = if (
            mode == CloudGenerationMode.CLOUD_PREFERRED &&
            aiConfig.cloudGenerationEnabled &&
            aiConfig.apiKey.isNotBlank()
        ) {
            runCatching {
                cloudQuizPackGenerator.generate(document, aiConfig, settings.questionsPerDifficulty)
            }
        } else {
            null
        }

        val generationResult = generatedPackMerger.mergeWithOfflineFallback(
            cloudPack = cloudResult?.getOrNull(),
            offlinePack = offlinePack,
            document = document,
            questionsPerDifficulty = settings.questionsPerDifficulty,
            cloudFailure = cloudResult?.exceptionOrNull()
        )
        val serializedPack = QuizConfigParser.serializeQuizPack(generationResult.pack)
        quizPackStorageDataSource.saveCustomPack(serializedPack, generationResult.pack.id)
        packCache[generationResult.pack.id] = generationResult.pack
        return generationResult
    }

    override suspend fun deleteCustomPack(packId: String) {
        if (packId == QuizPack.OFFICIAL_ALTAI_PACK_ID) return
        quizPackStorageDataSource.deleteCustomPack(packId)
        packCache.remove(packId)
    }

    override suspend fun getAiGenerationConfig(): AiGenerationConfig =
        quizPreferencesDataSource.readAiGenerationConfig()

    override suspend fun saveAiGenerationConfig(config: AiGenerationConfig) {
        quizPreferencesDataSource.saveAiGenerationConfig(config)
    }

    override suspend fun getPlayerProgress(): PlayerProgress =
        quizPreferencesDataSource.readPlayerProgress()

    override suspend fun saveSelectedTheme(themeId: String) {
        quizPreferencesDataSource.saveSelectedTheme(themeId)
    }

    override suspend fun saveQuizSettings(settings: QuizSettings) {
        quizPreferencesDataSource.saveUserSettings(settings)
    }

    override suspend fun persistRunOutcome(summary: RunSummary): PlayerProgress {
        val currentProgress = quizPreferencesDataSource.readPlayerProgress()
        val updatedProgress = currentProgress.copy(
            unlockedAtlasNodeIds = currentProgress.unlockedAtlasNodeIds + summary.unlockedNodeIds,
            unlockedAchievementIds = currentProgress.unlockedAchievementIds + summary.earnedAchievementIds,
            discoveredThemeIds = currentProgress.discoveredThemeIds + summary.themeId,
            bestRuns = (currentProgress.bestRuns + summary)
                .sortedWith(
                    compareByDescending<RunSummary> { it.score }
                        .thenByDescending { it.accuracyRatio }
                        .thenByDescending { it.timestamp }
                )
                .take(15),
            latestRun = summary
        )
        quizPreferencesDataSource.saveProgress(updatedProgress)
        return updatedProgress
    }

    override suspend fun resetProgress() {
        quizPreferencesDataSource.resetProgress()
    }

    private fun resolvePack(packId: String): QuizPack? {
        if (packId == QuizPack.OFFICIAL_ALTAI_PACK_ID) {
            return officialPack()
        }
        return packCache[packId]
    }

    private fun officialPack(): QuizPack {
        val official = packCache[QuizPack.OFFICIAL_ALTAI_PACK_ID] ?: localQuizDataSource.getOfficialPack()
        packCache[official.id] = official
        return official
    }
}
