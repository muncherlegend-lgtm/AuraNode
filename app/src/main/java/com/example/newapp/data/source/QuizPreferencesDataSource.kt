package com.example.newapp.data.source

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.newapp.data.model.Difficulty
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.quizPreferencesDataStore by preferencesDataStore(
    name = "aura_node_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

class QuizPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun readMergedSettings(defaults: QuizSettings): QuizSettings {
        val preferences = safePreferences()
        return mergeSettings(preferences, defaults)
    }

    suspend fun readPlayerProgress(): PlayerProgress {
        val preferences = safePreferences()
        return PlayerProgress(
            unlockedAtlasNodeIds = preferences[Keys.UNLOCKED_ATLAS_NODES].orEmpty(),
            unlockedAchievementIds = preferences[Keys.UNLOCKED_ACHIEVEMENTS].orEmpty(),
            discoveredThemeIds = preferences[Keys.DISCOVERED_THEMES].orEmpty(),
            bestRuns = preferences[Keys.BEST_RUNS_JSON]
                ?.let { rawValue ->
                    runCatching { decodeRunSummaries(rawValue) }
                        .onFailure { Log.w(TAG, "Failed to decode saved best runs.", it) }
                        .getOrDefault(emptyList())
                }
                .orEmpty(),
            latestRun = preferences[Keys.LATEST_RUN_JSON]
                ?.let { rawValue ->
                    runCatching { decodeRunSummary(rawValue) }
                        .onFailure { Log.w(TAG, "Failed to decode latest run.", it) }
                        .getOrNull()
                }
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
            preferences[Keys.TIMER_SECONDS] = settings.timerSeconds
            preferences[Keys.AUTO_ADVANCE_DELAY_MS] = settings.autoAdvanceDelayMs
            preferences[Keys.SHOW_TIMER] = settings.showTimer
            preferences[Keys.COMPACT_UI] = settings.compactUi
            preferences[Keys.MOTION_ENABLED] = settings.motionEnabled
            preferences[Keys.HAPTICS_ENABLED] = settings.hapticsEnabled
            preferences[Keys.SOUND_ENABLED] = settings.soundEnabled
            preferences[Keys.SHUFFLE_QUESTIONS] = settings.shuffleQuestions
            preferences[Keys.SHUFFLE_OPTIONS] = settings.shuffleOptions
            preferences[Keys.QUESTIONS_PER_DIFFICULTY] = settings.questionsPerDifficulty
            preferences[Keys.DEFAULT_MODE] = settings.defaultMode.name
            preferences[Keys.SELECTED_THEME_ID] = settings.defaultThemeId
            preferences[Keys.DEFAULT_PACK_ID] = settings.defaultPackId
        }
    }

    suspend fun saveProgress(progress: PlayerProgress) {
        context.quizPreferencesDataStore.edit { preferences ->
            preferences[Keys.UNLOCKED_ATLAS_NODES] = progress.unlockedAtlasNodeIds
            preferences[Keys.UNLOCKED_ACHIEVEMENTS] = progress.unlockedAchievementIds
            preferences[Keys.DISCOVERED_THEMES] = progress.discoveredThemeIds
            preferences[Keys.BEST_RUNS_JSON] = encodeRunSummaries(progress.bestRuns)
            if (progress.latestRun != null) {
                preferences[Keys.LATEST_RUN_JSON] = encodeRunSummary(progress.latestRun)
            } else {
                preferences.remove(Keys.LATEST_RUN_JSON)
            }
        }
    }

    suspend fun resetProgress() {
        context.quizPreferencesDataStore.edit { it.clearProgress() }
    }

    fun userSettingsFlow(defaults: QuizSettings) = context.quizPreferencesDataStore.data
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            Log.w(TAG, "Failed to observe preferences, emitting defaults.", throwable)
            emit(emptyPreferences())
        }
        .map { preferences -> mergeSettings(preferences, defaults) }

    private suspend fun safePreferences(): Preferences = context.quizPreferencesDataStore.data
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            Log.w(TAG, "Failed to read preferences, falling back to empty preferences.", throwable)
            emit(emptyPreferences())
        }
        .first()

    private fun mergeSettings(
        preferences: Preferences,
        defaults: QuizSettings
    ): QuizSettings = defaults.copy(
        timerSeconds = preferences[Keys.TIMER_SECONDS] ?: defaults.timerSeconds,
        autoAdvanceDelayMs = preferences[Keys.AUTO_ADVANCE_DELAY_MS] ?: defaults.autoAdvanceDelayMs,
        showTimer = preferences[Keys.SHOW_TIMER] ?: defaults.showTimer,
        compactUi = preferences[Keys.COMPACT_UI] ?: defaults.compactUi,
        motionEnabled = preferences[Keys.MOTION_ENABLED] ?: defaults.motionEnabled,
        hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: defaults.hapticsEnabled,
        soundEnabled = preferences[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
        shuffleQuestions = preferences[Keys.SHUFFLE_QUESTIONS] ?: defaults.shuffleQuestions,
        shuffleOptions = preferences[Keys.SHUFFLE_OPTIONS] ?: defaults.shuffleOptions,
        questionsPerDifficulty = preferences[Keys.QUESTIONS_PER_DIFFICULTY]
            ?: defaults.questionsPerDifficulty,
        defaultThemeId = preferences[Keys.SELECTED_THEME_ID] ?: defaults.defaultThemeId,
        defaultPackId = preferences[Keys.DEFAULT_PACK_ID] ?: defaults.defaultPackId,
        defaultMode = preferences[Keys.DEFAULT_MODE]
            ?.let { rawValue ->
                parseQuizMode(rawValue).also {
                    if (it.name != rawValue) {
                        Log.w(TAG, "Unknown saved quiz mode '$rawValue'. Falling back to ${it.name}.")
                    }
                }
            }
            ?: defaults.defaultMode,
    )

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
            timestamp = jsonObject.optLong("timestamp", System.currentTimeMillis()),
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
            difficulty = parseDifficulty(jsonObject.optString("difficulty")),
            mode = parseQuizMode(jsonObject.optString("mode")),
            themeId = jsonObject.optString("themeId", QuizSettings.DEFAULT_THEME_ID),
            score = jsonObject.optInt("score"),
            maxScore = jsonObject.optInt("maxScore"),
            correctAnswers = jsonObject.optInt("correctAnswers"),
            totalQuestions = jsonObject.optInt("totalQuestions"),
            accuracyRatio = jsonObject.optDouble("accuracyRatio").toFloat(),
            currentStreak = jsonObject.optInt("currentStreak"),
            longestStreak = jsonObject.optInt("longestStreak"),
            timeBonus = jsonObject.optInt("timeBonus"),
            medalTier = parseMedalTier(jsonObject.optString("medalTier")),
            unlockedNodeIds = jsonObject.optJSONArray("unlockedNodeIds")?.toStringList().orEmpty(),
            earnedAchievementIds = jsonObject.optJSONArray("earnedAchievementIds")?.toStringList().orEmpty()
        )
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    private fun parseQuizMode(rawValue: String): QuizMode = runCatching {
        QuizMode.valueOf(rawValue.ifBlank { QuizMode.CLASSIC.name })
    }.getOrDefault(QuizMode.CLASSIC)

    private fun parseDifficulty(rawValue: String): Difficulty = runCatching {
        Difficulty.valueOf(rawValue.ifBlank { Difficulty.CADET.name })
    }.getOrDefault(Difficulty.CADET)

    private fun parseMedalTier(rawValue: String): MedalTier = runCatching {
        MedalTier.valueOf(rawValue.ifBlank { MedalTier.NONE.name })
    }.getOrDefault(MedalTier.NONE)

    private object Keys {
        val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val TIMER_SECONDS = intPreferencesKey("timer_seconds")
        val AUTO_ADVANCE_DELAY_MS = longPreferencesKey("auto_advance_delay_ms")
        val SHOW_TIMER = booleanPreferencesKey("show_timer")
        val COMPACT_UI = booleanPreferencesKey("compact_ui")
        val MOTION_ENABLED = booleanPreferencesKey("motion_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SHUFFLE_QUESTIONS = booleanPreferencesKey("shuffle_questions")
        val SHUFFLE_OPTIONS = booleanPreferencesKey("shuffle_options")
        val QUESTIONS_PER_DIFFICULTY = intPreferencesKey("questions_per_difficulty")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val DEFAULT_PACK_ID = stringPreferencesKey("default_pack_id")
        val UNLOCKED_ATLAS_NODES = stringSetPreferencesKey("unlocked_atlas_nodes")
        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
        val DISCOVERED_THEMES = stringSetPreferencesKey("discovered_themes")
        val BEST_RUNS_JSON = stringPreferencesKey("best_runs_json")
        val LATEST_RUN_JSON = stringPreferencesKey("latest_run_json")
    }

    private companion object {
        const val TAG = "QuizPreferences"
    }
}
