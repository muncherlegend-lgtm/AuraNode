package com.example.newapp.ui.quiz

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newapp.data.model.AiGenerationConfig
import com.example.newapp.data.model.AiProvider
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.CloudGenerationMode
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.HomeContentPreference
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackSummary
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.repository.QuizRepository
import com.example.newapp.domain.usecase.AchievementEvaluatorUseCase
import com.example.newapp.domain.usecase.AtlasUnlockUseCase
import com.example.newapp.domain.usecase.LegendEligibilityUseCase
import com.example.newapp.domain.usecase.RunScoringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val runScoringUseCase: RunScoringUseCase,
    private val atlasUnlockUseCase: AtlasUnlockUseCase,
    private val achievementEvaluatorUseCase: AchievementEvaluatorUseCase,
    private val legendEligibilityUseCase: LegendEligibilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var advanceJob: Job? = null
    private var pendingImportedDocument: ImportedDocument? = null

    init {
        viewModelScope.launch {
            loadConfiguration()
        }
    }

    fun selectMode(mode: QuizMode) {
        val state = _uiState.value
        if (!state.isOfficialPackSelected && mode != QuizMode.CLASSIC) return
        if (mode == QuizMode.LEGEND && state.legendUnlockedDifficulties.isEmpty()) return
        updateQuizSettings {
            it.copy(
                defaultMode = mode,
                juryModeEnabled = it.juryModeEnabled && mode == QuizMode.CLASSIC
            )
        }
    }

    fun selectPack(packId: String) {
        val state = _uiState.value
        val targetPack = state.quizPacks.firstOrNull { it.id == packId } ?: return
        val resolvedMode = if (targetPack.type == QuizPackType.CUSTOM_IMPORTED) {
            QuizMode.CLASSIC
        } else {
            state.quizSettings.defaultMode
        }
        updateQuizSettings {
            it.copy(
                defaultPackId = packId,
                defaultMode = resolvedMode,
                juryModeEnabled = it.juryModeEnabled && targetPack.type == QuizPackType.OFFICIAL_ALTAI
            )
        }
        _uiState.update {
            it.copy(
                selectedPackId = packId,
                selectedMode = resolvedMode,
                generationErrorMessage = null
            )
        }
    }

    fun setHomeContentPreference(preference: HomeContentPreference) {
        updateQuizSettings { it.copy(homeContentPreference = preference) }
    }

    fun completeOnboarding() {
        updateQuizSettings { it.copy(hasCompletedOnboarding = true) }
    }

    fun prepareImportedDocument(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generationErrorMessage = null,
                    generationWarnings = emptyList()
                )
            }
            runCatching { quizRepository.prepareImportedDocument(uri) }
                .onSuccess { importedDocument ->
                    pendingImportedDocument = importedDocument
                    _uiState.update {
                        it.copy(
                            importedDocumentPreview = importedDocument,
                            generationErrorMessage = null,
                            generationWarnings = emptyList()
                        )
                    }
                }
                .onFailure { throwable ->
                    pendingImportedDocument = null
                    _uiState.update {
                        it.copy(
                            importedDocumentPreview = null,
                            generationErrorMessage = throwable.message ?: "Не удалось прочитать файл."
                        )
                    }
                }
        }
    }

    fun dismissImportedDocumentPreview() {
        pendingImportedDocument = null
        _uiState.update {
            it.copy(
                importedDocumentPreview = null,
                isAiConsentSheetVisible = false,
                generationWarnings = emptyList()
            )
        }
    }

    fun requestCloudPackGeneration() {
        val state = _uiState.value
        if (!state.cloudGenerationReady) {
            generateImportedPack(CloudGenerationMode.OFFLINE_ONLY)
            return
        }
        if (!state.aiGenerationConfig.hasCloudConsent) {
            _uiState.update { it.copy(isAiConsentSheetVisible = true) }
            return
        }
        generateImportedPack(CloudGenerationMode.CLOUD_PREFERRED)
    }

    fun confirmCloudConsentAndGenerate() {
        updateAiGenerationConfig { it.copy(hasCloudConsent = true) }
        _uiState.update { it.copy(isAiConsentSheetVisible = false) }
        generateImportedPack(CloudGenerationMode.CLOUD_PREFERRED)
    }

    fun dismissAiConsent() {
        _uiState.update { it.copy(isAiConsentSheetVisible = false) }
    }

    fun generateOfflinePack() {
        generateImportedPack(CloudGenerationMode.OFFLINE_ONLY)
    }

    fun deleteCustomPack(packId: String) {
        viewModelScope.launch {
            quizRepository.deleteCustomPack(packId)
            val refreshedPacks = quizRepository.getQuizPacks()
            val nextPackId = if (_uiState.value.selectedPackId == packId) {
                QuizPack.OFFICIAL_ALTAI_PACK_ID
            } else {
                _uiState.value.selectedPackId
            }
            updateQuizSettings { settings ->
                settings.copy(defaultPackId = nextPackId)
            }
            _uiState.update {
                it.copy(
                    quizPacks = refreshedPacks,
                    selectedPackId = nextPackId
                )
            }
        }
    }

    fun setAiProvider(provider: AiProvider) {
        updateAiGenerationConfig { it.copy(provider = provider) }
    }

    fun setCloudGenerationEnabled(enabled: Boolean) {
        updateAiGenerationConfig { it.copy(cloudGenerationEnabled = enabled) }
    }

    fun updateAiApiKey(apiKey: String) {
        updateAiGenerationConfig { it.copy(apiKey = apiKey) }
    }

    fun updateGeminiModel(model: String) {
        updateAiGenerationConfig { it.copy(geminiModel = model) }
    }

    fun updateOpenRouterModel(model: String) {
        updateAiGenerationConfig { it.copy(openRouterModel = model) }
    }

    fun dismissGenerationMessage() {
        _uiState.update {
            it.copy(
                generationErrorMessage = null,
                generationWarnings = emptyList()
            )
        }
    }

    fun startQuiz(difficulty: Difficulty) {
        val state = _uiState.value
        if (state.isLoading) return

        val selectedPack = state.selectedPack
        val mode = if (selectedPack?.type == QuizPackType.CUSTOM_IMPORTED) {
            QuizMode.CLASSIC
        } else {
            state.quizSettings.defaultMode
        }
        if (mode == QuizMode.LEGEND && !state.legendUnlockedDifficulties.contains(difficulty)) {
            return
        }

        cancelRunningJobs()
        val settings = state.quizSettings.sanitize()
        val questions = prepareQuestions(
            questions = quizRepository.getQuestionsByDifficulty(
                difficulty = difficulty,
                mode = mode,
                packId = state.selectedPackId
            ),
            settings = settings,
            mode = mode
        )

        _uiState.update {
            it.copy(
                selectedDifficulty = difficulty,
                selectedMode = mode,
                questions = questions,
                currentQuestionIndex = 0,
                score = 0,
                expectedMaxScore = calculateExpectedMaxScore(questions.size, settings.timerSeconds, mode),
                correctAnswersCount = 0,
                currentStreak = 0,
                longestStreak = 0,
                timeBonus = 0,
                timeoutCount = 0,
                isQuizCompleted = false,
                selectedAnswerIndex = null,
                revealedAnswerIndex = null,
                isAnswerLocked = false,
                answerFeedbackType = null,
                timerSecondsLeft = settings.timerSeconds,
                answerInput = "",
                submittedAnswerText = null,
                latestRunSummary = null,
                runUnlockedNodeIds = emptySet(),
                runEarnedAchievementIds = emptySet(),
                generationErrorMessage = null
            )
        }

        if (questions.isNotEmpty() && settings.showTimer) {
            startTimer(settings.timerSeconds)
        }
    }

    fun restartCurrentDifficulty() {
        val difficulty = _uiState.value.selectedDifficulty ?: return
        startQuiz(difficulty)
    }

    fun updateAnswerInput(value: String) {
        _uiState.update { state ->
            if (state.isAnswerLocked || state.isQuizCompleted) {
                state
            } else {
                state.copy(answerInput = value)
            }
        }
    }

    fun submitAnswer(answerIndex: Int) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.isAnswerLocked || state.isQuizCompleted) return
        if (!shouldShowOptions(state.quizSettings)) return

        handleAnswer(
            question = question,
            submittedAnswerIndex = answerIndex,
            submittedText = question.options.getOrNull(answerIndex),
            isCorrect = answerIndex == question.correctAnswerIndex
        )
    }

    fun submitTypedAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val submittedText = state.answerInput.trim()
        if (state.isAnswerLocked || state.isQuizCompleted || submittedText.isBlank()) return
        if (!shouldShowFreeText(state.quizSettings)) return

        handleAnswer(
            question = question,
            submittedAnswerIndex = null,
            submittedText = submittedText,
            isCorrect = isTextAnswerCorrect(question, submittedText)
        )
    }

    fun selectTheme(themeId: String) {
        if (_uiState.value.availableThemes.none { it.id == themeId }) {
            return
        }
        updateQuizSettings { it.copy(defaultThemeId = themeId) }
        _uiState.update { it.copy(selectedThemeId = themeId) }
        viewModelScope.launch {
            quizRepository.saveSelectedTheme(themeId)
        }
    }

    fun setTimerEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(showTimer = enabled) }
    }

    fun setCompactUiEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(compactUi = enabled) }
    }

    fun setShuffleQuestionsEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(shuffleQuestions = enabled) }
    }

    fun setMotionEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(motionEnabled = enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(soundEnabled = enabled) }
    }

    fun setJuryModeEnabled(enabled: Boolean) {
        updateQuizSettings { current ->
            if (enabled) {
                current.copy(
                    juryModeEnabled = true,
                    showTimer = true,
                    defaultMode = QuizMode.CLASSIC,
                    defaultPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
                    answerMode = AnswerMode.CLASSIC_OPTIONS,
                    allowOptionSelection = true,
                    allowFreeTextAnswers = false
                )
            } else {
                current.copy(juryModeEnabled = false)
            }
        }
        if (enabled) {
            _uiState.update {
                it.copy(
                    selectedPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
                    selectedMode = QuizMode.CLASSIC
                )
            }
        }
    }

    fun setDemoResetOnLaunch(enabled: Boolean) {
        updateQuizSettings { it.copy(demoResetOnLaunch = enabled) }
    }

    fun setAnswerMode(answerMode: AnswerMode) {
        updateQuizSettings {
            it.copy(
                answerMode = answerMode,
                allowFreeTextAnswers = answerMode != AnswerMode.CLASSIC_OPTIONS,
                juryModeEnabled = it.juryModeEnabled && answerMode == AnswerMode.CLASSIC_OPTIONS
            )
        }
    }

    fun resetDemoProgress() {
        cancelRunningJobs()
        viewModelScope.launch {
            quizRepository.resetProgress()
            val emptyProgress = PlayerProgress()
            _uiState.update { state ->
                state.copy(
                    selectedDifficulty = null,
                    questions = emptyList(),
                    currentQuestionIndex = 0,
                    score = 0,
                    expectedMaxScore = 0,
                    correctAnswersCount = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    timeBonus = 0,
                    timeoutCount = 0,
                    isQuizCompleted = false,
                    selectedAnswerIndex = null,
                    revealedAnswerIndex = null,
                    isAnswerLocked = false,
                    answerFeedbackType = null,
                    timerSecondsLeft = state.quizSettings.timerSeconds,
                    answerInput = "",
                    submittedAnswerText = null,
                    playerProgress = emptyProgress,
                    latestRunSummary = null,
                    runUnlockedNodeIds = emptySet(),
                    runEarnedAchievementIds = emptySet(),
                    legendUnlockedDifficulties = emptySet()
                )
            }
        }
    }

    fun resetQuiz() {
        cancelRunningJobs()
        _uiState.update { state ->
            state.copy(
                selectedDifficulty = null,
                questions = emptyList(),
                currentQuestionIndex = 0,
                score = 0,
                expectedMaxScore = 0,
                correctAnswersCount = 0,
                currentStreak = 0,
                longestStreak = 0,
                timeBonus = 0,
                timeoutCount = 0,
                isQuizCompleted = false,
                selectedAnswerIndex = null,
                revealedAnswerIndex = null,
                isAnswerLocked = false,
                answerFeedbackType = null,
                timerSecondsLeft = state.quizSettings.timerSeconds,
                answerInput = "",
                submittedAnswerText = null,
                latestRunSummary = null,
                runUnlockedNodeIds = emptySet(),
                runEarnedAchievementIds = emptySet()
            )
        }
    }

    private suspend fun loadConfiguration() {
        var recoveredFromStartupIssue = false
        val defaultSettings = quizRepository.getQuizConfig().sanitize()

        suspend fun <T> recover(
            label: String,
            fallback: T,
            block: suspend () -> T
        ): T = runCatching { block() }
            .onFailure {
                recoveredFromStartupIssue = true
                Log.w(TAG, "Failed to load $label. Using fallback value.", it)
            }
            .getOrDefault(fallback)

        val settings = recover("quiz settings", defaultSettings) {
            quizRepository.getQuizSettings().sanitize()
        }
        if (settings.demoResetOnLaunch) {
            runCatching { quizRepository.resetProgress() }
                .onFailure { Log.w(TAG, "Failed to reset progress on launch.", it) }
        }
        val progress = recover("player progress", PlayerProgress()) {
            quizRepository.getPlayerProgress()
        }
        val themes = recover("theme presets", quizRepository.getThemePresets()) {
            quizRepository.getThemePresets()
        }
        val atlasNodes = recover("atlas nodes", quizRepository.getAtlasNodes()) {
            quizRepository.getAtlasNodes()
        }
        val achievements = recover("achievements", quizRepository.getAchievements()) {
            quizRepository.getAchievements()
        }
        val packs = recover("quiz packs", emptyList()) {
            quizRepository.getQuizPacks()
        }
        val aiConfig = recover("AI config", AiGenerationConfig()) {
            quizRepository.getAiGenerationConfig()
        }
        val selectedThemeId = themes.firstOrNull { it.id == settings.defaultThemeId }?.id
            ?: themes.firstOrNull()?.id
            ?: settings.defaultThemeId
        val selectedPackId = resolveSelectedPackId(settings, packs)
        val safeSettings = settings.copy(defaultPackId = selectedPackId)
        if (safeSettings.defaultPackId != settings.defaultPackId) {
            quizRepository.saveQuizSettings(safeSettings)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                quizSettings = safeSettings,
                availableThemes = themes,
                selectedThemeId = selectedThemeId,
                quizPacks = packs,
                selectedPackId = selectedPackId,
                aiGenerationConfig = aiConfig,
                atlasNodes = atlasNodes,
                achievements = achievements,
                playerProgress = progress,
                latestRunSummary = progress.latestRun,
                legendUnlockedDifficulties = Difficulty.entries
                    .filter { difficulty -> legendEligibilityUseCase(progress, difficulty) }
                    .toSet(),
                timerSecondsLeft = safeSettings.timerSeconds,
                generationErrorMessage = if (recoveredFromStartupIssue) {
                    "Локальные данные были частично повреждены. Приложение запущено в безопасном режиме."
                } else {
                    null
                }
            )
        }
    }

    private fun generateImportedPack(mode: CloudGenerationMode) {
        val importedDocument = pendingImportedDocument ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingPack = true,
                    generationErrorMessage = null,
                    generationWarnings = emptyList()
                )
            }

            runCatching { quizRepository.generateImportedPack(importedDocument, mode) }
                .onSuccess { result ->
                    val refreshedPacks = quizRepository.getQuizPacks()
                    val updatedSettings = _uiState.value.quizSettings.copy(defaultPackId = result.pack.id)
                    quizRepository.saveQuizSettings(updatedSettings)
                    pendingImportedDocument = null
                    _uiState.update {
                        it.copy(
                            quizPacks = refreshedPacks,
                            selectedPackId = result.pack.id,
                            importedDocumentPreview = null,
                            isGeneratingPack = false,
                            generationWarnings = result.warnings,
                            generationErrorMessage = result.fallbackReason,
                            quizSettings = updatedSettings
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isGeneratingPack = false,
                            generationErrorMessage = throwable.message ?: "Не удалось сгенерировать набор."
                        )
                    }
                }
        }
    }

    private fun handleAnswer(
        question: Question,
        submittedAnswerIndex: Int?,
        submittedText: String?,
        isCorrect: Boolean
    ) {
        val state = _uiState.value
        timerJob?.cancel()

        val resolution = runScoringUseCase.resolveAnswer(
            mode = state.selectedMode,
            isCorrect = isCorrect,
            secondsLeft = state.timerSecondsLeft,
            currentStreak = state.currentStreak
        )
        val unlockedNodes = state.runUnlockedNodeIds + atlasUnlockUseCase.unlockForQuestion(question, isCorrect)
        val nextLongestStreak = maxOf(state.longestStreak, resolution.nextStreak)

        _uiState.update {
            it.copy(
                score = it.score + resolution.scoreDelta,
                correctAnswersCount = it.correctAnswersCount + if (isCorrect) 1 else 0,
                currentStreak = resolution.nextStreak,
                longestStreak = nextLongestStreak,
                timeBonus = it.timeBonus + resolution.timeBonusDelta,
                selectedAnswerIndex = submittedAnswerIndex,
                revealedAnswerIndex = question.correctAnswerIndex,
                isAnswerLocked = true,
                answerInput = "",
                submittedAnswerText = submittedText,
                runUnlockedNodeIds = unlockedNodes,
                answerFeedbackType = if (isCorrect) {
                    AnswerFeedbackType.CORRECT
                } else {
                    AnswerFeedbackType.INCORRECT
                }
            )
        }

        scheduleAdvance()
    }

    private fun startTimer(initialSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var secondsLeft = initialSeconds
            _uiState.update { it.copy(timerSecondsLeft = secondsLeft) }

            while (secondsLeft > 0 && isActive) {
                delay(1_000)
                val currentState = _uiState.value
                if (currentState.isAnswerLocked || currentState.isQuizCompleted || currentState.questions.isEmpty()) {
                    return@launch
                }

                secondsLeft -= 1
                _uiState.update { latest ->
                    if (latest.isAnswerLocked) {
                        latest
                    } else {
                        latest.copy(timerSecondsLeft = secondsLeft)
                    }
                }
            }

            if (_uiState.value.isAnswerLocked || _uiState.value.isQuizCompleted) {
                return@launch
            }

            revealTimedOutAnswer()
        }
    }

    private fun revealTimedOutAnswer() {
        val question = _uiState.value.currentQuestion ?: return
        _uiState.update {
            it.copy(
                currentStreak = 0,
                revealedAnswerIndex = question.correctAnswerIndex,
                isAnswerLocked = true,
                answerFeedbackType = AnswerFeedbackType.TIMEOUT,
                timerSecondsLeft = 0,
                timeoutCount = it.timeoutCount + 1,
                submittedAnswerText = null
            )
        }
        scheduleAdvance()
    }

    private fun scheduleAdvance() {
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(_uiState.value.quizSettings.autoAdvanceDelayMs)
            moveToNextQuestion()
        }
    }

    private fun moveToNextQuestion() {
        val state = _uiState.value
        val nextQuestionIndex = state.currentQuestionIndex + 1

        if (nextQuestionIndex >= state.questions.size) {
            completeRun()
            return
        }

        _uiState.update {
            it.copy(
                currentQuestionIndex = nextQuestionIndex,
                selectedAnswerIndex = null,
                revealedAnswerIndex = null,
                isAnswerLocked = false,
                answerFeedbackType = null,
                timerSecondsLeft = it.quizSettings.timerSeconds,
                answerInput = "",
                submittedAnswerText = null
            )
        }

        if (state.quizSettings.showTimer) {
            startTimer(state.quizSettings.timerSeconds)
        }
    }

    private fun completeRun() {
        cancelRunningJobs()
        viewModelScope.launch {
            val currentState = _uiState.value
            val difficulty = currentState.selectedDifficulty ?: return@launch
            val selectedPack = currentState.selectedPack
            val preliminarySummary = RunSummary(
                timestamp = System.currentTimeMillis(),
                packId = currentState.selectedPackId,
                packTitle = selectedPack?.title.orEmpty(),
                packType = selectedPack?.type ?: QuizPackType.OFFICIAL_ALTAI,
                packGenerationSource = selectedPack?.generationSource ?: PackGenerationSource.OFFICIAL,
                sourceFileName = selectedPack?.sourceFileName.orEmpty(),
                difficulty = difficulty,
                mode = currentState.selectedMode,
                themeId = currentState.selectedThemeId,
                score = currentState.score,
                maxScore = currentState.maxScore,
                correctAnswers = currentState.correctAnswersCount,
                totalQuestions = currentState.totalQuestions,
                accuracyRatio = runScoringUseCase.computeAccuracy(
                    currentState.correctAnswersCount,
                    currentState.totalQuestions
                ),
                currentStreak = currentState.currentStreak,
                longestStreak = currentState.longestStreak,
                timeBonus = currentState.timeBonus,
                medalTier = MedalTier.NONE,
                unlockedNodeIds = if (currentState.isOfficialPackSelected) {
                    currentState.runUnlockedNodeIds.toList()
                } else {
                    emptyList()
                },
                earnedAchievementIds = emptyList()
            )
            val medal = runScoringUseCase.calculateMedal(preliminarySummary)
            val summaryWithMedal = preliminarySummary.copy(medalTier = medal)
            val earnedAchievements = achievementEvaluatorUseCase(
                achievements = currentState.achievements,
                progress = currentState.playerProgress,
                runSummary = summaryWithMedal,
                atlasNodeCount = currentState.atlasNodes.size,
                themeCount = currentState.availableThemes.size,
                timeoutCount = currentState.timeoutCount
            )
            val finalSummary = summaryWithMedal.copy(
                earnedAchievementIds = earnedAchievements
            )
            val updatedProgress = quizRepository.persistRunOutcome(finalSummary)
            val updatedLegendUnlocks = Difficulty.entries
                .filter { targetDifficulty -> legendEligibilityUseCase(updatedProgress, targetDifficulty) }
                .toSet()

            _uiState.update {
                it.copy(
                    isQuizCompleted = true,
                    timerSecondsLeft = 0,
                    playerProgress = updatedProgress,
                    latestRunSummary = finalSummary,
                    runEarnedAchievementIds = earnedAchievements.toSet(),
                    legendUnlockedDifficulties = updatedLegendUnlocks
                )
            }
        }
    }

    private fun updateQuizSettings(transform: (QuizSettings) -> QuizSettings) {
        val updatedSettings = transform(_uiState.value.quizSettings).sanitize()
        _uiState.update { state ->
            state.copy(
                quizSettings = updatedSettings,
                selectedMode = updatedSettings.defaultMode,
                selectedThemeId = updatedSettings.defaultThemeId,
                selectedPackId = updatedSettings.defaultPackId,
                timerSecondsLeft = if (state.questions.isEmpty()) {
                    updatedSettings.timerSeconds
                } else {
                    state.timerSecondsLeft.coerceAtMost(updatedSettings.timerSeconds)
                }
            )
        }
        viewModelScope.launch {
            quizRepository.saveQuizSettings(updatedSettings)
        }
    }

    private fun updateAiGenerationConfig(transform: (AiGenerationConfig) -> AiGenerationConfig) {
        val updatedConfig = transform(_uiState.value.aiGenerationConfig)
        _uiState.update { it.copy(aiGenerationConfig = updatedConfig) }
        viewModelScope.launch {
            quizRepository.saveAiGenerationConfig(updatedConfig)
        }
    }

    private fun prepareQuestions(
        questions: List<Question>,
        settings: QuizSettings,
        mode: QuizMode
    ): List<Question> {
        val orderedQuestions = when {
            mode == QuizMode.LEGEND -> questions.sortedByDescending { it.difficultyWeight }
            settings.shuffleQuestions -> questions.shuffled()
            else -> questions.sortedBy { it.id }
        }

        val selectedQuestions = when (mode) {
            QuizMode.CLASSIC -> orderedQuestions.take(settings.questionsPerDifficulty)
            QuizMode.SPRINT -> orderedQuestions.take(settings.questionsPerDifficulty)
            QuizMode.LEGEND -> orderedQuestions.take(3)
        }

        return selectedQuestions.map { question ->
            if (settings.shuffleOptions) {
                shuffleQuestionOptions(question)
            } else {
                question
            }
        }
    }

    private fun shuffleQuestionOptions(question: Question): Question {
        val indexedOptions = question.options
            .mapIndexed { index, option -> index to option }
            .shuffled()
        val correctAnswerIndex = indexedOptions.indexOfFirst { it.first == question.correctAnswerIndex }
        return question.copy(
            options = indexedOptions.map { it.second },
            correctAnswerIndex = correctAnswerIndex
        )
    }

    private fun calculateExpectedMaxScore(
        questionCount: Int,
        timerSeconds: Int,
        mode: QuizMode
    ): Int = when (mode) {
        QuizMode.CLASSIC -> questionCount * 10
        QuizMode.SPRINT -> (1..questionCount).sumOf { 12 + timerSeconds + (it * 3) }
        QuizMode.LEGEND -> (1..questionCount).sumOf { 18 + (timerSeconds * 2) + (it * 4) }
    }

    private fun shouldShowOptions(settings: QuizSettings): Boolean = when (settings.answerMode) {
        AnswerMode.CLASSIC_OPTIONS -> true
        AnswerMode.EXPLORER_MIXED -> true
        AnswerMode.EXPLORER_TEXT -> false
    }

    private fun shouldShowFreeText(settings: QuizSettings): Boolean = when (settings.answerMode) {
        AnswerMode.CLASSIC_OPTIONS -> false
        AnswerMode.EXPLORER_MIXED -> true
        AnswerMode.EXPLORER_TEXT -> true
    }

    private fun isTextAnswerCorrect(question: Question, submittedText: String): Boolean {
        val normalizedInput = normalizeAnswer(submittedText)
        if (normalizedInput.isBlank()) return false

        val acceptedAnswers = buildList {
            add(question.options.getOrElse(question.correctAnswerIndex) { "" })
            addAll(question.acceptedAnswers)
            add((question.correctAnswerIndex + 1).toString())
        }.map(::normalizeAnswer).filter(String::isNotBlank)

        return acceptedAnswers.any { candidate -> normalizedInput == candidate }
    }

    private fun normalizeAnswer(source: String): String = source
        .lowercase()
        .replace('ё', 'е')
        .replace(NON_LETTER_PATTERN, " ")
        .replace(MULTIPLE_SPACES_PATTERN, " ")
        .trim()

    private fun resolveSelectedPackId(
        settings: QuizSettings,
        packs: List<QuizPackSummary>
    ): String {
        if (packs.any { it.id == settings.defaultPackId }) {
            return settings.defaultPackId
        }
        return when (settings.homeContentPreference) {
            HomeContentPreference.OFFICIAL_FIRST -> QuizPack.OFFICIAL_ALTAI_PACK_ID
            HomeContentPreference.CUSTOM_FIRST -> packs.firstOrNull { it.type == QuizPackType.CUSTOM_IMPORTED }?.id
                ?: QuizPack.OFFICIAL_ALTAI_PACK_ID

            HomeContentPreference.BALANCED -> packs.firstOrNull()?.id ?: QuizPack.OFFICIAL_ALTAI_PACK_ID
        }
    }

    private fun QuizSettings.sanitize(): QuizSettings {
        val safeOptionSelection = answerMode != AnswerMode.EXPLORER_TEXT
        val allowFreeText = answerMode != AnswerMode.CLASSIC_OPTIONS
        val sanitized = copy(
            timerSeconds = timerSeconds.coerceAtLeast(5),
            autoAdvanceDelayMs = autoAdvanceDelayMs.coerceAtLeast(800L),
            questionsPerDifficulty = questionsPerDifficulty.coerceAtLeast(1),
            allowOptionSelection = safeOptionSelection,
            allowFreeTextAnswers = allowFreeText,
            defaultThemeId = defaultThemeId.ifBlank { QuizSettings.DEFAULT_THEME_ID },
            defaultPackId = defaultPackId.ifBlank { QuizPack.OFFICIAL_ALTAI_PACK_ID }
        )
        return if (sanitized.juryModeEnabled) {
            sanitized.copy(
                showTimer = true,
                defaultMode = QuizMode.CLASSIC,
                defaultPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
                answerMode = AnswerMode.CLASSIC_OPTIONS,
                allowOptionSelection = true,
                allowFreeTextAnswers = false
            )
        } else {
            sanitized
        }
    }

    private fun cancelRunningJobs() {
        timerJob?.cancel()
        advanceJob?.cancel()
        timerJob = null
        advanceJob = null
    }

    override fun onCleared() {
        cancelRunningJobs()
        super.onCleared()
    }

    private companion object {
        const val TAG = "QuizViewModel"
        val NON_LETTER_PATTERN = "[^\\p{L}\\p{Nd}]+".toRegex()
        val MULTIPLE_SPACES_PATTERN = "\\s+".toRegex()
    }
}
