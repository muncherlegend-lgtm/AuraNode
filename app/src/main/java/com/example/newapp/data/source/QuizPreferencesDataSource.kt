package com.example.newapp.data.source

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.AiGenerationConfig
import com.example.newapp.data.model.AiProvider
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.HomeContentPreference
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.RunSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.quizPreferencesDataStore by preferencesDataStore(name = "aura_node_preferences")

class QuizPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val secureValueCipher: SecureValueCipher
) {

    suspend fun readMergedSettings(defaults: QuizSettings): QuizSettings {
        val preferences = context.quizPreferencesDataStore.data.first()
        return defaults.copy(
            showTimer = preferences[Keys.SHOW_TIMER] ?: defaults.showTimer,
            compactUi = preferences[Keys.COMPACT_UI] ?: defaults.compactUi,
            motionEnabled = preferences[Keys.MOTION_ENABLED] ?: defaults.motionEnabled,
            hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: defaults.hapticsEnabled,
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
            juryModeEnabled = preferences[Keys.JURY_MODE_ENABLED] ?: defaults.juryModeEnabled,
            demoResetOnLaunch = preferences[Keys.DEMO_RESET_ON_LAUNCH] ?: defaults.demoResetOnLaunch,
            defaultThemeId = preferences[Keys.SELECTED_THEME_ID] ?: defaults.defaultThemeId,
            defaultPackId = preferences[Keys.DEFAULT_PACK_ID] ?: defaults.defaultPackId,
            defaultMode = preferences[Keys.DEFAULT_MODE]?.let(QuizMode::valueOf) ?: defaults.defaultMode,
            answerMode = preferences[Keys.ANSWER_MODE]?.let(AnswerMode::valueOf) ?: defaults.answerMode,
            homeContentPreference = preferences[Keys.HOME_CONTENT_PREFERENCE]
                ?.let(HomeContentPreference::valueOf)
                ?: defaults.homeContentPreference,
            hasCompletedOnboarding = preferences[Keys.HAS_COMPLETED_ONBOARDING]
                ?: defaults.hasCompletedOnboarding
        )
    }

    suspend fun readPlayerProgress(): PlayerProgress {
        val preferences = context.quizPreferencesDataStore.data.first()
        return PlayerProgress(
            unlockedAtlasNodeIds = preferences[Keys.UNLOCKED_ATLAS_NODES].orEmpty(),
            unlockedAchievementIds = preferences[Keys.UNLOCKED_ACHIEVEMENTS].orEmpty(),
            discoveredThemeIds = preferences[Keys.DISCOVERED_THEMES].orEmpty(),
            bestRuns = preferences[Keys.BEST_RUNS_JSON]?.let(::decodeRunSummaries).orEmpty(),
            latestRun = preferences[Keys.LATEST_RUN_JSON]?.let(::decodeRunSummary)
        )
    }

    suspend fun saveSelectedTheme(themeId: String) {
        context.quizPreferencesDataStore.edit { preferences ->
            preferences[Keys.SELECTED_THEME_ID] = themeId
            val discovered = preferences[Keys.DISCOVERED_THEMES].orEmpty().toMutableSet()
            discovered += themeId
            preferences[Keys.DISCOVERED_THEMES] = discovered
        }
    }

    suspend fun saveUserSettings(settings: QuizSettings) {
        context.quizPreferencesDataStore.edit { preferences ->
            preferences[Keys.SHOW_TIMER] = settings.showTimer
            preferences[Keys.COMPACT_UI] = settings.compactUi
            preferences[Keys.MOTION_ENABLED] = settings.motionEnabled
            preferences[Keys.HAPTICS_ENABLED] = settings.hapticsEnabled
            preferences[Keys.SOUND_ENABLED] = settings.soundEnabled
            preferences[Keys.JURY_MODE_ENABLED] = settings.juryModeEnabled
            preferences[Keys.DEMO_RESET_ON_LAUNCH] = settings.demoResetOnLaunch
            preferences[Keys.DEFAULT_MODE] = settings.defaultMode.name
            preferences[Keys.ANSWER_MODE] = settings.answerMode.name
            preferences[Keys.SELECTED_THEME_ID] = settings.defaultThemeId
            preferences[Keys.DEFAULT_PACK_ID] = settings.defaultPackId
            preferences[Keys.HOME_CONTENT_PREFERENCE] = settings.homeContentPreference.name
            preferences[Keys.HAS_COMPLETED_ONBOARDING] = settings.hasCompletedOnboarding
        }
    }

    suspend fun readAiGenerationConfig(): AiGenerationConfig {
        val preferences = context.quizPreferencesDataStore.data.first()
        return AiGenerationConfig(
            provider = preferences[Keys.AI_PROVIDER]?.let(AiProvider::valueOf) ?: AiGenerationConfig().provider,
            apiKey = secureValueCipher.decrypt(preferences[Keys.AI_API_KEY].orEmpty()),
            cloudGenerationEnabled = preferences[Keys.AI_CLOUD_ENABLED]
                ?: AiGenerationConfig().cloudGenerationEnabled,
            hasCloudConsent = preferences[Keys.AI_HAS_CLOUD_CONSENT]
                ?: AiGenerationConfig().hasCloudConsent,
            geminiModel = preferences[Keys.AI_GEMINI_MODEL]
                ?: AiGenerationConfig.DEFAULT_GEMINI_MODEL,
            openRouterModel = preferences[Keys.AI_OPENROUTER_MODEL].orEmpty()
        )
    }

    suspend fun saveAiGenerationConfig(config: AiGenerationConfig) {
        context.quizPreferencesDataStore.edit { preferences ->
            preferences[Keys.AI_PROVIDER] = config.provider.name
            preferences[Keys.AI_API_KEY] = secureValueCipher.encrypt(config.apiKey)
            preferences[Keys.AI_CLOUD_ENABLED] = config.cloudGenerationEnabled
            preferences[Keys.AI_HAS_CLOUD_CONSENT] = config.hasCloudConsent
            preferences[Keys.AI_GEMINI_MODEL] = config.geminiModel
            preferences[Keys.AI_OPENROUTER_MODEL] = config.openRouterModel
        }
    }

    suspend fun saveProgress(progress: PlayerProgress) {
        context.quizPreferencesDataStore.edit { preferences ->
            preferences[Keys.UNLOCKED_ATLAS_NODES] = progress.unlockedAtlasNodeIds
            preferences[Keys.UNLOCKED_ACHIEVEMENTS] = progress.unlockedAchievementIds
            preferences[Keys.DISCOVERED_THEMES] = progress.discoveredThemeIds
            preferences[Keys.BEST_RUNS_JSON] = encodeRunSummaries(progress.bestRuns)
            progress.latestRun?.let { preferences[Keys.LATEST_RUN_JSON] = encodeRunSummary(it) }
        }
    }

    suspend fun resetProgress() {
        context.quizPreferencesDataStore.edit { it.clearProgress() }
    }

    fun userSettingsFlow(defaults: QuizSettings) = context.quizPreferencesDataStore.data.map { preferences ->
        defaults.copy(
            showTimer = preferences[Keys.SHOW_TIMER] ?: defaults.showTimer,
            compactUi = preferences[Keys.COMPACT_UI] ?: defaults.compactUi,
            motionEnabled = preferences[Keys.MOTION_ENABLED] ?: defaults.motionEnabled,
            hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: defaults.hapticsEnabled,
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
            juryModeEnabled = preferences[Keys.JURY_MODE_ENABLED] ?: defaults.juryModeEnabled,
            demoResetOnLaunch = preferences[Keys.DEMO_RESET_ON_LAUNCH] ?: defaults.demoResetOnLaunch,
            defaultThemeId = preferences[Keys.SELECTED_THEME_ID] ?: defaults.defaultThemeId,
            defaultPackId = preferences[Keys.DEFAULT_PACK_ID] ?: defaults.defaultPackId,
            defaultMode = preferences[Keys.DEFAULT_MODE]?.let(QuizMode::valueOf) ?: defaults.defaultMode,
            answerMode = preferences[Keys.ANSWER_MODE]?.let(AnswerMode::valueOf) ?: defaults.answerMode,
            homeContentPreference = preferences[Keys.HOME_CONTENT_PREFERENCE]
                ?.let(HomeContentPreference::valueOf)
                ?: defaults.homeContentPreference,
            hasCompletedOnboarding = preferences[Keys.HAS_COMPLETED_ONBOARDING]
                ?: defaults.hasCompletedOnboarding
        )
    }

    private fun MutablePreferences.clearProgress() {
        remove(Keys.UNLOCKED_ATLAS_NODES)
        remove(Keys.UNLOCKED_ACHIEVEMENTS)
        remove(Keys.DISCOVERED_THEMES)
        remove(Keys.BEST_RUNS_JSON)
        remove(Keys.LATEST_RUN_JSON)
    }

    private fun encodeRunSummary(summary: RunSummary): String = JSONObject().apply {
        put("timestamp", summary.timestamp)
        put("packId", summary.packId)
        put("packTitle", summary.packTitle)
        put("packType", summary.packType.name)
        put("packGenerationSource", summary.packGenerationSource.name)
        put("sourceFileName", summary.sourceFileName)
        put("difficulty", summary.difficulty.name)
        put("mode", summary.mode.name)
        put("themeId", summary.themeId)
        put("score", summary.score)
        put("maxScore", summary.maxScore)
        put("correctAnswers", summary.correctAnswers)
        put("totalQuestions", summary.totalQuestions)
        put("accuracyRatio", summary.accuracyRatio.toDouble())
        put("currentStreak", summary.currentStreak)
        put("longestStreak", summary.longestStreak)
        put("timeBonus", summary.timeBonus)
        put("medalTier", summary.medalTier.name)
        put("unlockedNodeIds", JSONArray(summary.unlockedNodeIds))
        put("earnedAchievementIds", JSONArray(summary.earnedAchievementIds))
    }.toString()

    private fun encodeRunSummaries(summaries: List<RunSummary>): String = JSONArray(
        summaries.map(::encodeRunSummary)
    ).toString()

    private fun decodeRunSummaries(rawValue: String): List<RunSummary> = JSONArray(rawValue).let { array ->
        buildList {
            for (index in 0 until array.length()) {
                add(decodeRunSummary(array.getString(index)))
            }
        }
    }

    private fun decodeRunSummary(rawValue: String): RunSummary {
        val jsonObject = JSONObject(rawValue)
        return RunSummary(
            timestamp = jsonObject.getLong("timestamp"),
            packId = jsonObject.optString("packId", QuizPack.OFFICIAL_ALTAI_PACK_ID),
            packTitle = jsonObject.optString("packTitle"),
            packType = jsonObject.optString("packType")
                .let { runCatching { QuizPackType.valueOf(it) }.getOrDefault(QuizPackType.OFFICIAL_ALTAI) },
            packGenerationSource = jsonObject.optString("packGenerationSource")
                .let {
                    runCatching { PackGenerationSource.valueOf(it) }
                        .getOrDefault(PackGenerationSource.OFFICIAL)
                },
            sourceFileName = jsonObject.optString("sourceFileName"),
            difficulty = Difficulty.valueOf(jsonObject.getString("difficulty")),
            mode = QuizMode.valueOf(jsonObject.getString("mode")),
            themeId = jsonObject.getString("themeId"),
            score = jsonObject.getInt("score"),
            maxScore = jsonObject.getInt("maxScore"),
            correctAnswers = jsonObject.getInt("correctAnswers"),
            totalQuestions = jsonObject.getInt("totalQuestions"),
            accuracyRatio = jsonObject.getDouble("accuracyRatio").toFloat(),
            currentStreak = jsonObject.getInt("currentStreak"),
            longestStreak = jsonObject.getInt("longestStreak"),
            timeBonus = jsonObject.getInt("timeBonus"),
            medalTier = MedalTier.valueOf(jsonObject.getString("medalTier")),
            unlockedNodeIds = jsonObject.getJSONArray("unlockedNodeIds").toStringList(),
            earnedAchievementIds = jsonObject.getJSONArray("earnedAchievementIds").toStringList()
        )
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    private object Keys {
        val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val SHOW_TIMER = booleanPreferencesKey("show_timer")
        val COMPACT_UI = booleanPreferencesKey("compact_ui")
        val MOTION_ENABLED = booleanPreferencesKey("motion_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val JURY_MODE_ENABLED = booleanPreferencesKey("jury_mode_enabled")
        val DEMO_RESET_ON_LAUNCH = booleanPreferencesKey("demo_reset_on_launch")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val ANSWER_MODE = stringPreferencesKey("answer_mode")
        val DEFAULT_PACK_ID = stringPreferencesKey("default_pack_id")
        val HOME_CONTENT_PREFERENCE = stringPreferencesKey("home_content_preference")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_CLOUD_ENABLED = booleanPreferencesKey("ai_cloud_enabled")
        val AI_HAS_CLOUD_CONSENT = booleanPreferencesKey("ai_has_cloud_consent")
        val AI_GEMINI_MODEL = stringPreferencesKey("ai_gemini_model")
        val AI_OPENROUTER_MODEL = stringPreferencesKey("ai_openrouter_model")
        val UNLOCKED_ATLAS_NODES = stringSetPreferencesKey("unlocked_atlas_nodes")
        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
        val DISCOVERED_THEMES = stringSetPreferencesKey("discovered_themes")
        val BEST_RUNS_JSON = stringPreferencesKey("best_runs_json")
        val LATEST_RUN_JSON = stringPreferencesKey("latest_run_json")
        val TOTAL_RUNS = intPreferencesKey("total_runs")
        val LAST_PLAYED_AT = longPreferencesKey("last_played_at")
    }
}
