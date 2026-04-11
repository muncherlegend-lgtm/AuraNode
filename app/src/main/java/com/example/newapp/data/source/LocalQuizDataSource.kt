package com.example.newapp.data.source

import android.content.Context
import android.util.Log
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.example.newapp.R
import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AchievementRuleType
import com.example.newapp.data.model.AnswerMode
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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

class LocalQuizDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun getQuestions(): List<Question> = runCatching {
        QuizConfigParser.parseQuestions(readAssetText(QUESTIONS_ASSET_PATH))
    }.getOrElse { throwable ->
        Log.w(TAG, "Failed to load questions from config, using fallback resources.", throwable)
        fallbackQuestions()
    }

    fun getOfficialPack(): QuizPack {
        val questions = getQuestions()
        return QuizPack(
            id = QuizPack.OFFICIAL_ALTAI_PACK_ID,
            title = "Altai Expedition",
            description = "Официальная офлайн-викторина об Алтайском крае и Барнауле.",
            type = QuizPackType.OFFICIAL_ALTAI,
            generationSource = PackGenerationSource.OFFICIAL,
            sourceFileName = "built-in",
            sourceMimeType = "application/json",
            coverFact = questions.firstOrNull()?.explanation.orEmpty(),
            questions = questions
        )
    }

    fun getQuizSettings(): QuizSettings = runCatching {
        QuizConfigParser.parseQuizSettings(readAssetText(SETTINGS_ASSET_PATH))
    }.getOrElse { throwable ->
        Log.w(TAG, "Failed to load quiz settings from config, using defaults.", throwable)
        QuizSettings()
    }.sanitize()

    fun getThemePresets(): List<ThemePreset> = runCatching {
        QuizConfigParser.parseThemePresets(readAssetText(THEMES_ASSET_PATH))
    }.getOrElse { throwable ->
        Log.w(TAG, "Failed to load theme presets from config, using fallback themes.", throwable)
        fallbackThemePresets()
    }.ifEmpty { fallbackThemePresets() }

    fun getAtlasNodes(): List<AtlasNode> = runCatching {
        QuizConfigParser.parseAtlasNodes(readAssetText(ATLAS_ASSET_PATH))
    }.getOrElse { throwable ->
        Log.w(TAG, "Failed to load atlas config, using fallback atlas.", throwable)
        fallbackAtlasNodes()
    }.ifEmpty { fallbackAtlasNodes() }

    fun getAchievements(): List<Achievement> = runCatching {
        QuizConfigParser.parseAchievements(readAssetText(ACHIEVEMENTS_ASSET_PATH))
    }.getOrElse { throwable ->
        Log.w(TAG, "Failed to load achievements config, using fallback achievements.", throwable)
        fallbackAchievements()
    }.ifEmpty { fallbackAchievements() }

    private fun parseQuestions(jsonText: String): List<Question> {
        val jsonArray = JSONObject(jsonText).getJSONArray("questions")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val options = item.optJSONArray("options")?.toStringList().orEmpty()
                val correctAnswerIndex = item.optInt("correctAnswerIndex", -1)
                if (options.size < 4 || correctAnswerIndex !in options.indices) {
                    Log.w(TAG, "Skipping malformed question at index=$index")
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

    private fun parseQuizSettings(jsonText: String): QuizSettings {
        val jsonObject = JSONObject(jsonText)
        return QuizSettings(
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
            defaultMode = parseQuizMode(jsonObject.optString("defaultMode")),
            answerMode = parseAnswerMode(jsonObject.optString("answerMode"))
        )
    }

    private fun parseThemePresets(jsonText: String): List<ThemePreset> {
        val jsonArray = JSONObject(jsonText).getJSONArray("themes")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val palette = item.getJSONObject("palette").toThemePalette()
                if (!palette.isValid()) {
                    Log.w(TAG, "Skipping theme ${item.optString("id")} due to invalid colors.")
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

    private fun parseAtlasNodes(jsonText: String): List<AtlasNode> {
        val jsonArray = JSONObject(jsonText).getJSONArray("nodes")
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val xFraction = item.optDouble("xFraction", 0.5).toFloat().coerceIn(0.05f, 0.95f)
                val yFraction = item.optDouble("yFraction", 0.5).toFloat().coerceIn(0.08f, 0.92f)
                add(
                    AtlasNode(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        subtitle = item.getString("subtitle"),
                        description = item.getString("description"),
                        highlightFact = item.getString("highlightFact"),
                        rewardTitle = item.getString("rewardTitle"),
                        factCategory = parseFactCategory(item.optString("factCategory")),
                        xFraction = xFraction,
                        yFraction = yFraction,
                        labelXFraction = item.optDouble("labelXFraction", xFraction.toDouble()).toFloat()
                            .coerceIn(0.05f, 0.95f),
                        labelYFraction = item.optDouble("labelYFraction", (yFraction - 0.09f).toDouble()).toFloat()
                            .coerceIn(0.05f, 0.95f),
                        connections = item.optJSONArray("connections")?.toStringList().orEmpty()
                    )
                )
            }
        }
    }

    private fun parseAchievements(jsonText: String): List<Achievement> {
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

    private fun fallbackQuestions(): List<Question> = questionDefinitions.map { definition ->
        Question(
            id = definition.id,
            text = context.getString(definition.textRes),
            options = context.resources.getStringArray(definition.optionsRes).toList().take(4),
            correctAnswerIndex = definition.correctAnswerIndex,
            difficulty = definition.difficulty,
            explanation = context.getString(definition.explanationRes),
            locationId = definition.locationId,
            factCategory = definition.factCategory,
            isLegendary = definition.isLegendary,
            difficultyWeight = definition.difficultyWeight,
            unlockReward = definition.unlockReward
        )
    }

    private fun fallbackThemePresets(): List<ThemePreset> = listOf(
        ThemePreset(
            id = "katun_dawn",
            title = "Катунь на рассвете",
            description = "Светлая бирюзово-золотая тема с мягкими волнами.",
            isDark = false,
            backgroundStyle = BackgroundArtworkStyle.WAVES,
            palette = themePalette(
                "#0B5561", "#FFFFFF", "#D8A449", "#241A00", "#5F7E71", "#FFFFFF",
                "#F7F2E8", "#1B2627", "#FFFDF7", "#1B2627", "#DCEAE8", "#425A5C",
                "#C9ECE9", "#032F35", "#FFE8BA", "#3D2A06", "#D8EADD", "#16311D", "#6D8183"
            )
        ),
        ThemePreset(
            id = "steppe_gold",
            title = "Степное золото",
            description = "Тёплая палитра полей и мягкий силуэт хребтов.",
            isDark = false,
            backgroundStyle = BackgroundArtworkStyle.MOUNTAINS,
            palette = themePalette(
                "#6A7B43", "#FFFFFF", "#C7882F", "#2A1800", "#8B5A2B", "#FFFFFF",
                "#FCF6EA", "#2C2318", "#FFF9F0", "#2C2318", "#EBDDC7", "#655748",
                "#E0E6C6", "#233007", "#F8DEB3", "#412600", "#EFD1BA", "#41220A", "#8C7A64"
            )
        ),
        ThemePreset(
            id = "cosmos_night",
            title = "Космос над Алтаем",
            description = "Тёмная сцена со звёздным фоном и холодным свечением.",
            isDark = true,
            backgroundStyle = BackgroundArtworkStyle.CONSTELLATION,
            palette = themePalette(
                "#85D7D2", "#062226", "#E0B45D", "#2B1A00", "#A7C5A6", "#122513",
                "#08151C", "#F4F0E5", "#11242C", "#F4F0E5", "#203A44", "#C9DDE0",
                "#0F3942", "#A7E7E2", "#4A3310", "#FDE6BA", "#28402E", "#DBEADC", "#6E878D"
            )
        ),
        ThemePreset(
            id = "river_mint",
            title = "Мятная пойма",
            description = "Светлая северная палитра с прохладными акцентами и туманом.",
            isDark = false,
            backgroundStyle = BackgroundArtworkStyle.WAVES,
            palette = themePalette(
                "#2E6F6F", "#FFFFFF", "#9BBF6B", "#132000", "#5A8D8D", "#FFFFFF",
                "#F4FAF8", "#173133", "#FCFFFE", "#173133", "#D8ECE8", "#4A6164",
                "#C2E7E1", "#08373B", "#E4F0C4", "#243100", "#D1ECE8", "#113333", "#7A9291"
            )
        ),
        ThemePreset(
            id = "sunset_copper",
            title = "Медный закат",
            description = "Контрастная закатная тема с тёплым свечением и дальними вершинами.",
            isDark = false,
            backgroundStyle = BackgroundArtworkStyle.MOUNTAINS,
            palette = themePalette(
                "#8A4A2B", "#FFFFFF", "#D79B35", "#2C1600", "#7D6A3A", "#FFFFFF",
                "#FBF1E8", "#2A211B", "#FFF9F2", "#2A211B", "#F0D9C6", "#6B564A",
                "#F3D0BB", "#4B1F08", "#F7E1B8", "#422600", "#E4E0B9", "#333013", "#8F7B6B"
            )
        )
    )

    private fun fallbackAtlasNodes(): List<AtlasNode> = listOf(
        AtlasNode("barnaul", "Барнаул", "Столица края", "Город сереброплавильного завода и культурный центр современного Алтая.", "Барнаул остаётся административным центром Алтайского края с 1937 года.", "Открыт исторический центр", FactCategory.HISTORY, 0.18f, 0.68f, 0.12f, 0.80f, listOf("biysk", "belokurikha", "kulunda")),
        AtlasNode("biysk", "Бийск", "Наукоград", "Один из главных научно-промышленных центров Алтая и ворота на Чуйский тракт.", "Бийск имеет статус наукограда Российской Федерации.", "Открыт научный маршрут", FactCategory.SCIENCE, 0.34f, 0.62f, 0.28f, 0.72f, listOf("barnaul", "chuysky_tract", "denisova_cave")),
        AtlasNode("belokurikha", "Белокуриха", "Курорт", "Термальный курорт с мягким предгорным климатом и санаторной славой.", "Белокуриха известна природными термальными источниками.", "Открыт курортный кластер", FactCategory.TRAVEL, 0.44f, 0.72f, 0.48f, 0.84f, listOf("barnaul", "biysk")),
        AtlasNode("denisova_cave", "Денисова пещера", "Археология", "Одна из самых известных археологических точек мира на территории Алтая.", "Именно здесь были сделаны открытия, изменившие представление об эволюции человека.", "Открыт древний слой", FactCategory.SCIENCE, 0.62f, 0.46f, 0.72f, 0.38f, listOf("biysk", "tigirek")),
        AtlasNode("tigirek", "Тигирекский заповедник", "Западный Алтай", "Охраняемые горные экосистемы юго-запада края и редкие виды животных.", "Заповедник хранит природные комплексы Западного Алтая.", "Открыта заповедная зона", FactCategory.NATURE, 0.72f, 0.30f, 0.81f, 0.19f, listOf("denisova_cave", "kolyvan")),
        AtlasNode("chuysky_tract", "Чуйский тракт", "Путь экспедиции", "Легендарная трасса Р-256, связывающая равнину, предгорья и Алтайские маршруты.", "Чуйский тракт входит в число самых известных дорог России.", "Открыта главная магистраль", FactCategory.TRAVEL, 0.56f, 0.58f, 0.61f, 0.70f, listOf("biysk", "kulunda")),
        AtlasNode("kulunda", "Кулундинская степь", "Степной запад", "Просторная степная территория с солёными озёрами и аграрной мощью.", "Кулундинская степь формирует особый природный образ западной части края.", "Открыт степной горизонт", FactCategory.NATURE, 0.20f, 0.30f, 0.14f, 0.18f, listOf("barnaul", "chuysky_tract")),
        AtlasNode("kolyvan", "Колывань", "Камнерезная школа", "Историческое село, связанное с камнерезным искусством и яшмовыми шедеврами.", "Колыванская фабрика прославилась «Царицей ваз» в Эрмитаже.", "Открыта камнерезная легенда", FactCategory.CULTURE, 0.84f, 0.22f, 0.82f, 0.10f, listOf("tigirek"))
    )

    private fun fallbackAchievements(): List<Achievement> = listOf(
        Achievement("perfect_run", "Безупречный маршрут", "Пройти любой уровень без ошибок.", "military_tech", AchievementRuleType.PERFECT_RUN),
        Achievement("no_timeouts", "Ритм экспедиции", "Завершить забег без единого таймаута.", "timer", AchievementRuleType.NO_TIMEOUTS),
        Achievement("streak_5", "Серия исследователя", "Собрать серию из 5 правильных ответов подряд.", "whatshot", AchievementRuleType.STREAK, threshold = 5),
        Achievement("atlas_master", "Хранитель Атласа", "Открыть все ключевые точки Алтайского атласа.", "public", AchievementRuleType.ATLAS_NODES, threshold = 8),
        Achievement("theme_nomad", "Коллекционер тем", "Попробовать все темы оформления.", "palette", AchievementRuleType.ALL_THEMES_TRIED, threshold = 5),
        Achievement("legend_clear", "Легенда Алтая", "Успешно завершить режим Legend.", "auto_awesome", AchievementRuleType.LEGEND_CLEAR)
    )

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

    private fun parseBackgroundStyle(rawValue: String): BackgroundArtworkStyle = runCatching {
        BackgroundArtworkStyle.valueOf(rawValue.ifBlank { BackgroundArtworkStyle.WAVES.name })
    }.getOrDefault(BackgroundArtworkStyle.WAVES)

    private fun parseAchievementRuleType(rawValue: String): AchievementRuleType = runCatching {
        AchievementRuleType.valueOf(rawValue.ifBlank { AchievementRuleType.PERFECT_RUN.name })
    }.getOrDefault(AchievementRuleType.PERFECT_RUN)

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

    private fun ThemePalette.isValid(): Boolean = listOf(
        primary,
        onPrimary,
        secondary,
        onSecondary,
        tertiary,
        onTertiary,
        background,
        onBackground,
        surface,
        onSurface,
        surfaceVariant,
        onSurfaceVariant,
        primaryContainer,
        onPrimaryContainer,
        secondaryContainer,
        onSecondaryContainer,
        tertiaryContainer,
        onTertiaryContainer,
        outline
    ).all { HEX_COLOR_REGEX.matches(it) }

    private fun themePalette(
        primary: String,
        onPrimary: String,
        secondary: String,
        onSecondary: String,
        tertiary: String,
        onTertiary: String,
        background: String,
        onBackground: String,
        surface: String,
        onSurface: String,
        surfaceVariant: String,
        onSurfaceVariant: String,
        primaryContainer: String,
        onPrimaryContainer: String,
        secondaryContainer: String,
        onSecondaryContainer: String,
        tertiaryContainer: String,
        onTertiaryContainer: String,
        outline: String
    ) = ThemePalette(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        outline = outline
    )

    private fun readAssetText(assetPath: String): String =
        context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun QuizSettings.sanitize(): QuizSettings {
        val sanitizedTimer = timerSeconds.coerceAtLeast(5)
        val sanitizedAutoAdvance = autoAdvanceDelayMs.coerceAtLeast(800L)
        val sanitizedQuestionCount = questionsPerDifficulty.coerceAtLeast(1)
        val sanitizedOptionSelection = if (!allowOptionSelection && !allowFreeTextAnswers) {
            true
        } else {
            allowOptionSelection
        }

        val sanitized = copy(
            timerSeconds = sanitizedTimer,
            autoAdvanceDelayMs = sanitizedAutoAdvance,
            questionsPerDifficulty = sanitizedQuestionCount,
            allowOptionSelection = sanitizedOptionSelection,
            defaultThemeId = defaultThemeId.ifBlank { QuizSettings.DEFAULT_THEME_ID }
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

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    private val questionDefinitions = listOf(
        QuestionDefinition(1, R.string.question_1_text, R.array.question_1_options, 1, Difficulty.CADET, R.string.question_1_explanation, "barnaul", FactCategory.HISTORY, false, 1, "Открыт исторический центр"),
        QuestionDefinition(2, R.string.question_2_text, R.array.question_2_options, 2, Difficulty.CADET, R.string.question_2_explanation, "belokurikha", FactCategory.TRAVEL, false, 1, "Открыт курортный кластер"),
        QuestionDefinition(3, R.string.question_3_text, R.array.question_3_options, 0, Difficulty.CADET, R.string.question_3_explanation, "barnaul", FactCategory.NATURE, false, 1, "Открыт речной узел"),
        QuestionDefinition(4, R.string.question_4_text, R.array.question_4_options, 1, Difficulty.CADET, R.string.question_4_explanation, "kulunda", FactCategory.INDUSTRY, true, 2, "Открыт аграрный горизонт"),
        QuestionDefinition(5, R.string.question_5_text, R.array.question_5_options, 2, Difficulty.CADET, R.string.question_5_explanation, "kulunda", FactCategory.HISTORY, true, 2, "Открыт административный маршрут"),
        QuestionDefinition(6, R.string.question_6_text, R.array.question_6_options, 1, Difficulty.ENGINEER, R.string.question_6_explanation, "barnaul", FactCategory.HISTORY, false, 2, "Открыт временной слой"),
        QuestionDefinition(7, R.string.question_7_text, R.array.question_7_options, 1, Difficulty.ENGINEER, R.string.question_7_explanation, "barnaul", FactCategory.INDUSTRY, false, 2, "Открыта демидовская линия"),
        QuestionDefinition(8, R.string.question_8_text, R.array.question_8_options, 0, Difficulty.ENGINEER, R.string.question_8_explanation, "biysk", FactCategory.SCIENCE, true, 3, "Открыт наукоград"),
        QuestionDefinition(9, R.string.question_9_text, R.array.question_9_options, 1, Difficulty.ENGINEER, R.string.question_9_explanation, "kulunda", FactCategory.NATURE, false, 2, "Открыт лесной феномен"),
        QuestionDefinition(10, R.string.question_10_text, R.array.question_10_options, 1, Difficulty.ENGINEER, R.string.question_10_explanation, "denisova_cave", FactCategory.SCIENCE, true, 4, "Открыт древний слой"),
        QuestionDefinition(11, R.string.question_11_text, R.array.question_11_options, 1, Difficulty.COSMONAUT, R.string.question_11_explanation, "tigirek", FactCategory.NATURE, true, 4, "Открыта заповедная зона"),
        QuestionDefinition(12, R.string.question_12_text, R.array.question_12_options, 1, Difficulty.COSMONAUT, R.string.question_12_explanation, "chuysky_tract", FactCategory.TRAVEL, true, 4, "Открыта главная магистраль"),
        QuestionDefinition(13, R.string.question_13_text, R.array.question_13_options, 0, Difficulty.COSMONAUT, R.string.question_13_explanation, "kulunda", FactCategory.NATURE, false, 3, "Открыт степной горизонт"),
        QuestionDefinition(14, R.string.question_14_text, R.array.question_14_options, 2, Difficulty.COSMONAUT, R.string.question_14_explanation, "belokurikha", FactCategory.NATURE, false, 3, "Открыто солёное озеро"),
        QuestionDefinition(15, R.string.question_15_text, R.array.question_15_options, 1, Difficulty.COSMONAUT, R.string.question_15_explanation, "kolyvan", FactCategory.CULTURE, true, 5, "Открыта камнерезная легенда")
    )

    private data class QuestionDefinition(
        val id: Int,
        @param:StringRes val textRes: Int,
        @param:ArrayRes val optionsRes: Int,
        val correctAnswerIndex: Int,
        val difficulty: Difficulty,
        @param:StringRes val explanationRes: Int,
        val locationId: String,
        val factCategory: FactCategory,
        val isLegendary: Boolean,
        val difficultyWeight: Int,
        val unlockReward: String
    )

    private companion object {
        const val TAG = "LocalQuizDataSource"
        const val QUESTIONS_ASSET_PATH = "config/questions.json"
        const val SETTINGS_ASSET_PATH = "config/quiz_settings.json"
        const val THEMES_ASSET_PATH = "config/themes.json"
        const val ATLAS_ASSET_PATH = "config/atlas.json"
        const val ACHIEVEMENTS_ASSET_PATH = "config/achievements.json"
        val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")
    }
}
