package com.example.newapp.data.source

import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AchievementRuleType
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.HomeContentPreference
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.BackgroundArtworkStyle
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.FactCategory
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.ThemePalette
import com.example.newapp.data.model.ThemePreset
import org.json.JSONArray
import org.json.JSONObject

internal object QuizConfigParser {

    fun parseQuestions(jsonText: String): List<Question> {
        val jsonArray = JSONObject(jsonText).getJSONArray("questions")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val options = item.optJSONArray("options")?.toStringList().orEmpty()
                val correctAnswerIndex = item.optInt("correctAnswerIndex", -1)
                if (options.size < 4 || correctAnswerIndex !in options.indices) {
                    continue
                }

                add(
                    Question(
                        id = item.getInt("id"),
                        text = item.getString("text"),
                        options = options.take(4),
                        correctAnswerIndex = correctAnswerIndex,
                        difficulty = parseDifficulty(item.optString("difficulty")),
                        explanation = item.optString("explanation"),
                        acceptedAnswers = item.optJSONArray("acceptedAnswers")?.toStringList().orEmpty(),
                        locationId = item.optString("locationId"),
                        factCategory = parseFactCategory(item.optString("factCategory")),
                        isLegendary = item.optBoolean("isLegendary", false),
                        difficultyWeight = item.optInt("difficultyWeight", 1).coerceAtLeast(1),
                        unlockReward = item.optString("unlockReward")
                    )
                )
            }
        }.sortedBy { it.id }
    }

    fun parseQuizSettings(jsonText: String): QuizSettings = sanitizeQuizSettings(
        JSONObject(jsonText).let { jsonObject ->
            QuizSettings(
                timerSeconds = jsonObject.optInt("timerSeconds", QuizSettings.DEFAULT_TIMER_SECONDS),
                autoAdvanceDelayMs = jsonObject.optLong(
                    "autoAdvanceDelayMs",
                    QuizSettings.DEFAULT_AUTO_ADVANCE_DELAY_MS
                ),
                showTimer = jsonObject.optBoolean("showTimer", true),
                allowOptionSelection = jsonObject.optBoolean("allowOptionSelection", true),
                allowFreeTextAnswers = jsonObject.optBoolean("allowFreeTextAnswers", false),
                compactUi = jsonObject.optBoolean("compactUi", true),
                motionEnabled = jsonObject.optBoolean("motionEnabled", true),
                hapticsEnabled = jsonObject.optBoolean("hapticsEnabled", true),
                soundEnabled = jsonObject.optBoolean("soundEnabled", false),
                juryModeEnabled = jsonObject.optBoolean("juryModeEnabled", true),
                demoResetOnLaunch = jsonObject.optBoolean("demoResetOnLaunch", false),
                shuffleQuestions = jsonObject.optBoolean("shuffleQuestions", false),
                shuffleOptions = jsonObject.optBoolean("shuffleOptions", false),
                questionsPerDifficulty = jsonObject.optInt(
                    "questionsPerDifficulty",
                    QuizSettings.DEFAULT_QUESTIONS_PER_DIFFICULTY
                ),
                defaultThemeId = jsonObject.optString("defaultThemeId", QuizSettings.DEFAULT_THEME_ID),
                defaultPackId = jsonObject.optString("defaultPackId", QuizPack.OFFICIAL_ALTAI_PACK_ID),
                defaultMode = parseQuizMode(jsonObject.optString("defaultMode")),
                answerMode = parseAnswerMode(jsonObject.optString("answerMode")),
                homeContentPreference = parseHomeContentPreference(
                    jsonObject.optString("homeContentPreference")
                ),
                hasCompletedOnboarding = jsonObject.optBoolean("hasCompletedOnboarding", false)
            )
        }
    )

    fun parseThemePresets(jsonText: String): List<ThemePreset> {
        val jsonArray = JSONObject(jsonText).getJSONArray("themes")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val palette = item.getJSONObject("palette").toThemePalette()
                if (!isValidPalette(palette)) {
                    continue
                }

                add(
                    ThemePreset(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        description = item.getString("description"),
                        isDark = item.optBoolean("isDark", false),
                        backgroundStyle = parseBackgroundStyle(item.optString("backgroundStyle")),
                        palette = palette
                    )
                )
            }
        }
    }

    fun parseAtlasNodes(jsonText: String): List<AtlasNode> {
        val jsonArray = JSONObject(jsonText).getJSONArray("nodes")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(
                    AtlasNode(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        subtitle = item.getString("subtitle"),
                        description = item.getString("description"),
                        highlightFact = item.getString("highlightFact"),
                        rewardTitle = item.getString("rewardTitle"),
                        factCategory = parseFactCategory(item.optString("factCategory")),
                        xFraction = item.optDouble("xFraction", 0.5).toFloat().coerceIn(0.05f, 0.95f),
                        yFraction = item.optDouble("yFraction", 0.5).toFloat().coerceIn(0.08f, 0.92f),
                        connections = item.optJSONArray("connections")?.toStringList().orEmpty()
                    )
                )
            }
        }
    }

    fun parseAchievements(jsonText: String): List<Achievement> {
        val jsonArray = JSONObject(jsonText).getJSONArray("achievements")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(
                    Achievement(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        description = item.getString("description"),
                        iconName = item.optString("iconName", "award"),
                        ruleType = parseAchievementRuleType(item.optString("ruleType")),
                        threshold = item.optInt("threshold", 1).coerceAtLeast(1)
                    )
                )
            }
        }
    }

    fun sanitizeQuizSettings(settings: QuizSettings): QuizSettings {
        val sanitizedTimer = settings.timerSeconds.coerceAtLeast(5)
        val sanitizedAutoAdvance = settings.autoAdvanceDelayMs.coerceAtLeast(800L)
        val sanitizedQuestionCount = settings.questionsPerDifficulty.coerceAtLeast(1)
        val sanitizedOptionSelection = if (!settings.allowOptionSelection && !settings.allowFreeTextAnswers) {
            true
        } else {
            settings.allowOptionSelection
        }

        val sanitized = settings.copy(
            timerSeconds = sanitizedTimer,
            autoAdvanceDelayMs = sanitizedAutoAdvance,
            questionsPerDifficulty = sanitizedQuestionCount,
            allowOptionSelection = sanitizedOptionSelection,
            defaultThemeId = settings.defaultThemeId.ifBlank { QuizSettings.DEFAULT_THEME_ID },
            defaultPackId = settings.defaultPackId.ifBlank { QuizPack.OFFICIAL_ALTAI_PACK_ID }
        )
        return if (sanitized.juryModeEnabled) {
            sanitized.copy(
                showTimer = true,
                defaultMode = QuizMode.CLASSIC,
                answerMode = AnswerMode.CLASSIC_OPTIONS,
                allowOptionSelection = true,
                allowFreeTextAnswers = false
            )
        } else {
            sanitized
        }
    }

    fun parseQuizPack(jsonText: String): QuizPack {
        val jsonObject = JSONObject(jsonText)
        return QuizPack(
            id = jsonObject.getString("id"),
            title = jsonObject.getString("title"),
            description = jsonObject.optString("description"),
            type = parseQuizPackType(jsonObject.optString("type")),
            generationSource = parsePackGenerationSource(jsonObject.optString("generationSource")),
            sourceFileName = jsonObject.optString("sourceFileName"),
            sourceMimeType = jsonObject.optString("sourceMimeType"),
            createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis()),
            coverFact = jsonObject.optString("coverFact"),
            questions = parseQuestions(
                JSONObject().put("questions", jsonObject.optJSONArray("questions") ?: JSONArray()).toString()
            )
        )
    }

    fun serializeQuizPack(pack: QuizPack): String = JSONObject().apply {
        put("id", pack.id)
        put("title", pack.title)
        put("description", pack.description)
        put("type", pack.type.name)
        put("generationSource", pack.generationSource.name)
        put("sourceFileName", pack.sourceFileName)
        put("sourceMimeType", pack.sourceMimeType)
        put("createdAt", pack.createdAt)
        put("coverFact", pack.coverFact)
        put("questions", JSONArray(pack.questions.map(::serializeQuestion)))
    }.toString()

    fun isValidPalette(palette: ThemePalette): Boolean = listOf(
        palette.primary,
        palette.onPrimary,
        palette.secondary,
        palette.onSecondary,
        palette.tertiary,
        palette.onTertiary,
        palette.background,
        palette.onBackground,
        palette.surface,
        palette.onSurface,
        palette.surfaceVariant,
        palette.onSurfaceVariant,
        palette.primaryContainer,
        palette.onPrimaryContainer,
        palette.secondaryContainer,
        palette.onSecondaryContainer,
        palette.tertiaryContainer,
        palette.onTertiaryContainer,
        palette.outline
    ).all { HEX_COLOR_REGEX.matches(it) }

    private fun parseDifficulty(rawValue: String): Difficulty = runCatching {
        Difficulty.valueOf(rawValue.ifBlank { Difficulty.CADET.name })
    }.getOrDefault(Difficulty.CADET)

    private fun parseFactCategory(rawValue: String): FactCategory = runCatching {
        FactCategory.valueOf(rawValue.ifBlank { FactCategory.HISTORY.name })
    }.getOrDefault(FactCategory.HISTORY)

    private fun parseQuizMode(rawValue: String): QuizMode = runCatching {
        QuizMode.valueOf(rawValue.ifBlank { QuizMode.CLASSIC.name })
    }.getOrDefault(QuizMode.CLASSIC)

    private fun parseAnswerMode(rawValue: String): AnswerMode = runCatching {
        AnswerMode.valueOf(rawValue.ifBlank { AnswerMode.CLASSIC_OPTIONS.name })
    }.getOrDefault(AnswerMode.CLASSIC_OPTIONS)

    private fun parseHomeContentPreference(rawValue: String): HomeContentPreference = runCatching {
        HomeContentPreference.valueOf(rawValue.ifBlank { HomeContentPreference.OFFICIAL_FIRST.name })
    }.getOrDefault(HomeContentPreference.OFFICIAL_FIRST)

    private fun parseBackgroundStyle(rawValue: String): BackgroundArtworkStyle = runCatching {
        BackgroundArtworkStyle.valueOf(rawValue.ifBlank { BackgroundArtworkStyle.WAVES.name })
    }.getOrDefault(BackgroundArtworkStyle.WAVES)

    private fun parseAchievementRuleType(rawValue: String): AchievementRuleType = runCatching {
        AchievementRuleType.valueOf(rawValue.ifBlank { AchievementRuleType.PERFECT_RUN.name })
    }.getOrDefault(AchievementRuleType.PERFECT_RUN)

    private fun parseQuizPackType(rawValue: String): QuizPackType = runCatching {
        QuizPackType.valueOf(rawValue.ifBlank { QuizPackType.CUSTOM_IMPORTED.name })
    }.getOrDefault(QuizPackType.CUSTOM_IMPORTED)

    private fun parsePackGenerationSource(rawValue: String): PackGenerationSource = runCatching {
        PackGenerationSource.valueOf(rawValue.ifBlank { PackGenerationSource.OFFLINE_DRAFT.name })
    }.getOrDefault(PackGenerationSource.OFFLINE_DRAFT)

    private fun JSONObject.toThemePalette(): ThemePalette = ThemePalette(
        primary = getString("primary"),
        onPrimary = getString("onPrimary"),
        secondary = getString("secondary"),
        onSecondary = getString("onSecondary"),
        tertiary = getString("tertiary"),
        onTertiary = getString("onTertiary"),
        background = getString("background"),
        onBackground = getString("onBackground"),
        surface = getString("surface"),
        onSurface = getString("onSurface"),
        surfaceVariant = getString("surfaceVariant"),
        onSurfaceVariant = getString("onSurfaceVariant"),
        primaryContainer = getString("primaryContainer"),
        onPrimaryContainer = getString("onPrimaryContainer"),
        secondaryContainer = getString("secondaryContainer"),
        onSecondaryContainer = getString("onSecondaryContainer"),
        tertiaryContainer = getString("tertiaryContainer"),
        onTertiaryContainer = getString("onTertiaryContainer"),
        outline = getString("outline")
    )

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    private fun serializeQuestion(question: Question): JSONObject = JSONObject().apply {
        put("id", question.id)
        put("text", question.text)
        put("options", JSONArray(question.options))
        put("correctAnswerIndex", question.correctAnswerIndex)
        put("difficulty", question.difficulty.name)
        put("explanation", question.explanation)
        put("acceptedAnswers", JSONArray(question.acceptedAnswers))
        put("locationId", question.locationId)
        put("factCategory", question.factCategory.name)
        put("isLegendary", question.isLegendary)
        put("difficultyWeight", question.difficultyWeight)
        put("unlockReward", question.unlockReward)
    }

    private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")
}
