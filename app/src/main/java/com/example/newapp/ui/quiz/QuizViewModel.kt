package com.example.newapp.ui.quiz

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.HomeContentPreference
import com.example.newapp.data.model.ImportedDocumentDraft
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuestionDraft
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
import com.example.newapp.ui.atlas.AtlasPanelMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    init {
        viewModelScope.launch {
            loadConfiguration()
        }
    }

    fun setSelectedDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun selectMode(mode: QuizMode) {
        val currentState = _uiState.value
        if (!canSelectMode(mode, currentState)) return
        _uiState.update { it.copy(selectedMode = mode) }
        persistSettings { settings -> settings.copy(defaultMode = mode) }
    }

    fun selectPack(packId: String) {
        val currentState = _uiState.value
        val pack = currentState.quizPacks.firstOrNull { it.id == packId } ?: return
        val resolvedMode = if (pack.type == QuizPackType.CUSTOM_IMPORTED) {
            QuizMode.CLASSIC
        } else {
            currentState.selectedMode
        }
        _uiState.update {
            it.copy(
                selectedPackId = packId,
                selectedMode = resolvedMode,
                generationErrorMessage = null
            )
        }
        persistSettings { settings ->
            settings.copy(
                defaultPackId = packId,
                defaultMode = resolvedMode
            )
        }
    }

    fun startOfficialQuiz() {
        val selectedDifficulty = _uiState.value.selectedDifficulty ?: Difficulty.CADET
        _uiState.update {
            it.copy(
                selectedPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
                selectedMode = it.selectedMode
            )
        }
        startQuiz(
            difficulty = selectedDifficulty,
            packId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
            requestedMode = _uiState.value.selectedMode
        )
    }

    fun startSelectedPackQuiz() {
        val currentState = _uiState.value
        startQuiz(
            difficulty = currentState.selectedDifficulty ?: Difficulty.CADET,
            packId = currentState.selectedPackId,
            requestedMode = currentState.selectedMode
        )
    }

    fun startPackQuiz(packId: String, difficulty: Difficulty) {
        selectPack(packId)
        startQuiz(
            difficulty = difficulty,
            packId = packId,
            requestedMode = QuizMode.CLASSIC
        )
    }

    fun restartCurrentDifficulty() {
        val currentState = _uiState.value
        startQuiz(
            difficulty = currentState.selectedDifficulty ?: Difficulty.CADET,
            packId = currentState.selectedPackId,
            requestedMode = currentState.selectedMode
        )
    }

    fun submitAnswer(answerIndex: Int) {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion ?: return
        if (currentState.isAnswerLocked) return

        timerJob?.cancel()

        val isTimeout = answerIndex !in currentQuestion.options.indices
        val isCorrect = !isTimeout && answerIndex == currentQuestion.correctAnswerIndex
        val resolution = runScoringUseCase.resolveAnswer(
            mode = currentState.selectedMode,
            isCorrect = isCorrect,
            secondsLeft = currentState.timerSecondsLeft,
            currentStreak = currentState.currentStreak
        )
        val unlockedNodeIds = if (currentState.isOfficialPackSelected) {
            atlasUnlockUseCase.unlockForQuestion(currentQuestion, isCorrect)
        } else {
            emptySet()
        }

        _uiState.update { state ->
            val updatedLongestStreak = maxOf(state.longestStreak, resolution.nextStreak)
            state.copy(
                score = state.score + resolution.scoreDelta,
                correctAnswersCount = state.correctAnswersCount + if (isCorrect) 1 else 0,
                currentStreak = resolution.nextStreak,
                longestStreak = updatedLongestStreak,
                timeBonus = state.timeBonus + resolution.timeBonusDelta,
                timeoutCount = state.timeoutCount + if (isTimeout) 1 else 0,
                selectedAnswerIndex = answerIndex.takeIf { it >= 0 },
                revealedAnswerIndex = currentQuestion.correctAnswerIndex,
                isAnswerLocked = true,
                answerFeedbackType = when {
                    isCorrect -> AnswerFeedbackType.CORRECT
                    isTimeout -> AnswerFeedbackType.TIMEOUT
                    else -> AnswerFeedbackType.INCORRECT
                },
                runUnlockedNodeIds = state.runUnlockedNodeIds + unlockedNodeIds,
                latestUnlockedAtlasNodeId = unlockedNodeIds.lastOrNull() ?: state.latestUnlockedAtlasNodeId
            )
        }

        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(currentState.quizSettings.autoAdvanceDelayMs)
            moveToNextQuestionOrFinish()
        }
    }

    fun resetQuiz() {
        cancelRunningJobs()
        _uiState.update {
            it.copy(
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
                timerSecondsLeft = it.quizSettings.timerSeconds,
                runUnlockedNodeIds = emptySet(),
                runEarnedAchievementIds = emptySet()
            )
        }
    }

    fun selectAtlasNode(nodeId: String) {
        _uiState.update {
            it.copy(
                selectedAtlasNodeId = nodeId,
                atlasPanelMode = AtlasPanelMode.NODE_DETAILS
            )
        }
    }

    fun prepareAtlasFromMenu() {
        _uiState.update {
            it.copy(
                selectedAtlasNodeId = null,
                atlasPanelMode = AtlasPanelMode.HIDDEN
            )
        }
    }

    fun focusLatestUnlockedAtlasNode() {
        val currentState = _uiState.value
        val targetNodeId = currentState.latestUnlockedAtlasNodeId
            ?: currentState.unlockedAtlasNodes.lastOrNull()?.id
            ?: return
        _uiState.update {
            it.copy(
                selectedAtlasNodeId = targetNodeId,
                atlasFocusRequestId = it.atlasFocusRequestId + 1
            )
        }
    }

    fun openAtlasFromResult() {
        val currentState = _uiState.value
        val targetNodeId = currentState.latestRunSummary?.unlockedNodeIds?.lastOrNull()
            ?: currentState.latestUnlockedAtlasNodeId
            ?: currentState.unlockedAtlasNodes.lastOrNull()?.id

        _uiState.update {
            if (targetNodeId != null) {
                it.copy(
                    selectedAtlasNodeId = targetNodeId,
                    atlasPanelMode = AtlasPanelMode.NODE_DETAILS,
                    atlasFocusRequestId = it.atlasFocusRequestId + 1
                )
            } else {
                it.copy(
                    selectedAtlasNodeId = null,
                    atlasPanelMode = AtlasPanelMode.EXPEDITION_PROGRESS
                )
            }
        }
    }

    fun setAtlasPanelMode(mode: AtlasPanelMode) {
        _uiState.update { it.copy(atlasPanelMode = mode) }
    }

    fun hideAtlasPanel() {
        _uiState.update { it.copy(atlasPanelMode = AtlasPanelMode.HIDDEN) }
    }

    fun selectTheme(themeId: String) {
        if (_uiState.value.availableThemes.none { it.id == themeId }) return
        _uiState.update {
            it.copy(
                selectedThemeId = themeId,
                quizSettings = it.quizSettings.copy(defaultThemeId = themeId)
            )
        }
        viewModelScope.launch {
            quizRepository.saveSelectedTheme(themeId)
            persistSettings { it.copy(defaultThemeId = themeId) }
        }
    }

    fun setTimerEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(showTimer = enabled) }
    }

    fun setTimerSeconds(seconds: Int) {
        updateQuizSettings { it.copy(timerSeconds = seconds.coerceIn(8, 60)) }
    }

    fun setQuestionsPerDifficulty(count: Int) {
        updateQuizSettings { it.copy(questionsPerDifficulty = count.coerceIn(3, 10)) }
    }

    fun setCompactUiEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(compactUi = enabled) }
    }

    fun setShuffleQuestionsEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(shuffleQuestions = enabled) }
    }

    fun setShuffleOptionsEnabled(enabled: Boolean) {
        updateQuizSettings { it.copy(shuffleOptions = enabled) }
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

    fun prepareImportedDocument(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingPack = true,
                    generationErrorMessage = null
                )
            }
            runCatching {
                val document = quizRepository.prepareImportedDocument(uri)
                quizRepository.createImportedDraft(
                    document = document,
                    questionsPerDifficulty = _uiState.value.quizSettings.questionsPerDifficulty
                )
            }.onSuccess { draft ->
                _uiState.update {
                    it.copy(
                        importedDocumentPreview = draft.document,
                        importedDraft = draft,
                        isGeneratingPack = false,
                        generationErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        importedDocumentPreview = null,
                        importedDraft = null,
                        isGeneratingPack = false,
                        generationErrorMessage = throwable.message ?: "Не удалось обработать файл."
                    )
                }
            }
        }
    }

    fun clearImportedDraft() {
        _uiState.update {
            it.copy(
                importedDocumentPreview = null,
                importedDraft = null,
                generationWarnings = emptyList(),
                generationErrorMessage = null
            )
        }
    }

    fun rebuildImportedDraftQuestions() {
        val currentDraft = _uiState.value.importedDraft ?: return
        if (currentDraft.includedSections.isEmpty()) {
            _uiState.update {
                it.copy(generationErrorMessage = "Выберите хотя бы один раздел для построения вопросов.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPack = true, generationErrorMessage = null) }
            runCatching {
                quizRepository.rebuildImportedDraftQuestions(
                    draft = currentDraft,
                    questionsPerDifficulty = _uiState.value.quizSettings.questionsPerDifficulty
                )
            }.onSuccess { questions ->
                _uiState.update { state ->
                    state.copy(
                        importedDraft = state.importedDraft?.copy(questionDrafts = questions),
                        isGeneratingPack = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isGeneratingPack = false,
                        generationErrorMessage = throwable.message ?: "Не удалось обновить черновик."
                    )
                }
            }
        }
    }

    fun updateDraftTitle(title: String) {
        mutateDraft { it.copy(title = title) }
    }

    fun updateDraftDescription(description: String) {
        mutateDraft { it.copy(description = description) }
    }

    fun toggleDraftSection(sectionId: String) {
        mutateDraft { draft ->
            draft.copy(
                sections = draft.sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(included = !section.included)
                    } else {
                        section
                    }
                }
            )
        }
    }

    fun addDraftQuestion() {
        mutateDraft { draft ->
            draft.copy(
                questionDrafts = draft.questionDrafts + QuestionDraft(
                    id = "draft_${System.currentTimeMillis()}_${draft.questionDrafts.size}",
                    text = "",
                    options = List(4) { "" },
                    correctAnswerIndex = 0,
                    difficulty = Difficulty.CADET,
                    explanation = ""
                )
            )
        }
    }

    fun removeDraftQuestion(questionId: String) {
        mutateDraft { draft ->
            draft.copy(questionDrafts = draft.questionDrafts.filterNot { it.id == questionId })
        }
    }

    fun updateDraftQuestionText(questionId: String, value: String) {
        updateDraftQuestion(questionId) { it.copy(text = value) }
    }

    fun updateDraftQuestionOption(questionId: String, optionIndex: Int, value: String) {
        updateDraftQuestion(questionId) { draft ->
            val updatedOptions = draft.options.toMutableList()
            if (optionIndex in updatedOptions.indices) {
                updatedOptions[optionIndex] = value
            }
            draft.copy(options = updatedOptions)
        }
    }

    fun updateDraftQuestionCorrectAnswer(questionId: String, correctAnswerIndex: Int) {
        updateDraftQuestion(questionId) {
            it.copy(correctAnswerIndex = correctAnswerIndex.coerceIn(0, 3))
        }
    }

    fun updateDraftQuestionDifficulty(questionId: String, difficulty: Difficulty) {
        updateDraftQuestion(questionId) { it.copy(difficulty = difficulty) }
    }

    fun updateDraftQuestionExplanation(questionId: String, value: String) {
        updateDraftQuestion(questionId) { it.copy(explanation = value) }
    }

    fun saveImportedDraft() {
        val currentDraft = _uiState.value.importedDraft ?: return
        if (currentDraft.questionDrafts.none { it.isValid() }) {
            _uiState.update {
                it.copy(generationErrorMessage = "Заполните хотя бы один корректный вопрос перед сохранением.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPack = true, generationErrorMessage = null) }
            runCatching { quizRepository.saveImportedDraft(currentDraft) }
                .onSuccess { savedPack ->
                    val packs = sanitizePackSummaries(quizRepository.getQuizPacks())
                    _uiState.update { state ->
                        state.copy(
                            quizPacks = packs,
                            selectedPackId = savedPack.id,
                            selectedMode = QuizMode.CLASSIC,
                            importedDocumentPreview = null,
                            importedDraft = null,
                            isGeneratingPack = false,
                            generationErrorMessage = "Набор сохранён в разделе «Материалы»."
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isGeneratingPack = false,
                            generationErrorMessage = throwable.message ?: "Не удалось сохранить набор."
                        )
                    }
                }
        }
    }

    fun deleteCustomPack(packId: String) {
        viewModelScope.launch {
            quizRepository.deleteCustomPack(packId)
            val refreshedPacks = sanitizePackSummaries(quizRepository.getQuizPacks())
            val nextSelectedPackId = if (_uiState.value.selectedPackId == packId) {
                QuizPack.OFFICIAL_ALTAI_PACK_ID
            } else {
                _uiState.value.selectedPackId
            }
            _uiState.update {
                it.copy(
                    quizPacks = refreshedPacks,
                    selectedPackId = nextSelectedPackId,
                    selectedMode = if (nextSelectedPackId == QuizPack.OFFICIAL_ALTAI_PACK_ID) {
                        it.selectedMode
                    } else {
                        QuizMode.CLASSIC
                    }
                )
            }
        }
    }

    fun deleteAllCustomPacks() {
        viewModelScope.launch {
            quizRepository.deleteAllCustomPacks()
            val refreshedPacks = sanitizePackSummaries(quizRepository.getQuizPacks())
            _uiState.update {
                it.copy(
                    quizPacks = refreshedPacks,
                    selectedPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
                    selectedMode = QuizMode.CLASSIC,
                    generationErrorMessage = "Пользовательские наборы удалены."
                )
            }
        }
    }

    fun clearResultsAndProgress() {
        viewModelScope.launch {
            quizRepository.resetProgress()
            val updatedProgress = quizRepository.getPlayerProgress()
            _uiState.update {
                it.copy(
                    playerProgress = updatedProgress,
                    latestRunSummary = null,
                    runUnlockedNodeIds = emptySet(),
                    runEarnedAchievementIds = emptySet(),
                    latestUnlockedAtlasNodeId = null,
                    selectedAtlasNodeId = null,
                    atlasPanelMode = AtlasPanelMode.HIDDEN,
                    legendUnlockedDifficulties = emptySet(),
                    generationErrorMessage = "Локальные результаты очищены."
                )
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(generationErrorMessage = null) }
    }

    private suspend fun loadConfiguration() {
        val defaultSettings = normalizeSettings(quizRepository.getQuizConfig())
        val persistedSettings = runCatching { normalizeSettings(quizRepository.getQuizSettings()) }
            .getOrElse {
                Log.w(TAG, "Failed to read persisted settings, using defaults.", it)
                defaultSettings
            }
        val themes = quizRepository.getThemePresets()
        val packs = sanitizePackSummaries(quizRepository.getQuizPacks())
        val atlasNodes = quizRepository.getAtlasNodes()
        val achievements = quizRepository.getAchievements()
        val progress = quizRepository.getPlayerProgress()
        val selectedThemeId = themes.firstOrNull { it.id == persistedSettings.defaultThemeId }?.id
            ?: themes.firstOrNull()?.id
            ?: QuizSettings.DEFAULT_THEME_ID
        val selectedDifficulty = Difficulty.CADET
        val selectedPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID
        val selectedMode = persistedSettings.defaultMode
            .takeIf {
                canSelectMode(
                    mode = it,
                    selectedPackType = QuizPackType.OFFICIAL_ALTAI,
                    legendUnlocked = progress
                )
            }
            ?: QuizMode.CLASSIC
        val legendUnlocked = Difficulty.entries
            .filter { difficulty -> legendEligibilityUseCase(progress, difficulty) }
            .toSet()

        val normalizedSettings = persistedSettings.copy(
            defaultThemeId = selectedThemeId,
            defaultPackId = selectedPackId,
            defaultMode = selectedMode
        )

        _uiState.value = QuizUiState(
            isLoading = false,
            selectedDifficulty = selectedDifficulty,
            selectedMode = selectedMode,
            timerSecondsLeft = normalizedSettings.timerSeconds,
            quizSettings = normalizedSettings,
            availableThemes = themes,
            selectedThemeId = selectedThemeId,
            quizPacks = packs,
            selectedPackId = selectedPackId,
            atlasNodes = atlasNodes,
            achievements = achievements,
            playerProgress = progress,
            latestRunSummary = progress.latestRun,
            latestUnlockedAtlasNodeId = progress.latestRun?.unlockedNodeIds?.lastOrNull()
                ?: progress.unlockedAtlasNodeIds.lastOrNull(),
            legendUnlockedDifficulties = legendUnlocked,
            atlasPanelMode = AtlasPanelMode.HIDDEN
        )

        persistSettings { normalizedSettings }
    }

    private fun startQuiz(
        difficulty: Difficulty,
        packId: String,
        requestedMode: QuizMode
    ) {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        cancelRunningJobs()

        val selectedPack = currentState.quizPacks.firstOrNull { it.id == packId }
        val resolvedMode = if (selectedPack?.type == QuizPackType.CUSTOM_IMPORTED) {
            QuizMode.CLASSIC
        } else {
            requestedMode
        }
        if (!canSelectMode(resolvedMode, currentState)) return

        val settings = normalizeSettings(currentState.quizSettings)
        val questions = prepareQuestions(
            questions = quizRepository.getQuestionsByDifficulty(
                difficulty = difficulty,
                mode = resolvedMode,
                packId = packId
            ),
            settings = settings,
            mode = resolvedMode
        )

        if (questions.isEmpty()) {
            _uiState.update {
                it.copy(generationErrorMessage = "Для выбранного уровня пока нет вопросов.")
            }
            return
        }

        _uiState.update {
            it.copy(
                selectedDifficulty = difficulty,
                selectedMode = resolvedMode,
                selectedPackId = packId,
                questions = questions,
                currentQuestionIndex = 0,
                score = 0,
                expectedMaxScore = calculateExpectedMaxScore(
                    totalQuestions = questions.size,
                    timerSeconds = settings.timerSeconds,
                    mode = resolvedMode
                ),
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
                runUnlockedNodeIds = emptySet(),
                runEarnedAchievementIds = emptySet()
            )
        }

        persistSettings {
            it.copy(
                defaultMode = resolvedMode,
                defaultPackId = packId
            )
        }

        startTimerForCurrentQuestion()
    }

    private fun moveToNextQuestionOrFinish() {
        val state = _uiState.value
        if (state.currentQuestionIndex >= state.questions.lastIndex) {
            completeQuiz()
        } else {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswerIndex = null,
                    revealedAnswerIndex = null,
                    isAnswerLocked = false,
                    answerFeedbackType = null,
                    timerSecondsLeft = it.quizSettings.timerSeconds
                )
            }
            startTimerForCurrentQuestion()
        }
    }

    private fun completeQuiz() {
        cancelRunningJobs()
        viewModelScope.launch {
            val currentState = _uiState.value
            val selectedPack = currentState.selectedPack
            if (currentState.questions.isEmpty()) return@launch

            val initialSummary = RunSummary(
                timestamp = System.currentTimeMillis(),
                packId = selectedPack?.id ?: QuizPack.OFFICIAL_ALTAI_PACK_ID,
                packTitle = selectedPack?.title.orEmpty(),
                packType = selectedPack?.type ?: QuizPackType.OFFICIAL_ALTAI,
                packGenerationSource = selectedPack?.generationSource ?: PackGenerationSource.OFFICIAL,
                sourceFileName = selectedPack?.sourceFileName.orEmpty(),
                difficulty = currentState.selectedDifficulty ?: Difficulty.CADET,
                mode = currentState.selectedMode,
                themeId = currentState.selectedThemeId,
                score = currentState.score,
                maxScore = currentState.maxScore,
                correctAnswers = currentState.correctAnswersCount,
                totalQuestions = currentState.questions.size,
                accuracyRatio = runScoringUseCase.computeAccuracy(
                    correctAnswers = currentState.correctAnswersCount,
                    totalQuestions = currentState.questions.size
                ),
                currentStreak = currentState.currentStreak,
                longestStreak = currentState.longestStreak,
                timeBonus = currentState.timeBonus,
                medalTier = MedalTier.NONE,
                unlockedNodeIds = currentState.runUnlockedNodeIds.toList(),
                earnedAchievementIds = emptyList()
            )

            val scoredSummary = initialSummary.copy(
                medalTier = runScoringUseCase.calculateMedal(initialSummary)
            )
            val earnedAchievementIds = achievementEvaluatorUseCase(
                achievements = currentState.achievements,
                progress = currentState.playerProgress,
                runSummary = scoredSummary,
                atlasNodeCount = currentState.atlasNodes.size,
                themeCount = currentState.availableThemes.size,
                timeoutCount = currentState.timeoutCount
            )
            val finalSummary = scoredSummary.copy(earnedAchievementIds = earnedAchievementIds)
            val updatedProgress = quizRepository.persistRunOutcome(finalSummary)
            val updatedLegendUnlocks = Difficulty.entries
                .filter { difficulty -> legendEligibilityUseCase(updatedProgress, difficulty) }
                .toSet()

            _uiState.update {
                it.copy(
                    isQuizCompleted = true,
                    latestRunSummary = finalSummary,
                    playerProgress = updatedProgress,
                    runEarnedAchievementIds = earnedAchievementIds.toSet(),
                    latestUnlockedAtlasNodeId = finalSummary.unlockedNodeIds.lastOrNull()
                        ?: it.latestUnlockedAtlasNodeId,
                    legendUnlockedDifficulties = updatedLegendUnlocks,
                    atlasFocusRequestId = if (finalSummary.unlockedNodeIds.isNotEmpty()) {
                        it.atlasFocusRequestId + 1
                    } else {
                        it.atlasFocusRequestId
                    }
                )
            }
        }
    }

    private fun startTimerForCurrentQuestion() {
        timerJob?.cancel()
        val settings = _uiState.value.quizSettings
        _uiState.update { it.copy(timerSecondsLeft = settings.timerSeconds) }
        if (!settings.showTimer) return

        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSecondsLeft > 0 && !_uiState.value.isAnswerLocked) {
                delay(1_000L)
                _uiState.update { state ->
                    state.copy(timerSecondsLeft = (state.timerSecondsLeft - 1).coerceAtLeast(0))
                }
            }
            if (!_uiState.value.isAnswerLocked && _uiState.value.timerSecondsLeft <= 0) {
                submitAnswer(-1)
            }
        }
    }

    private fun cancelRunningJobs() {
        timerJob?.cancel()
        advanceJob?.cancel()
        timerJob = null
        advanceJob = null
    }

    private fun prepareQuestions(
        questions: List<Question>,
        settings: QuizSettings,
        mode: QuizMode
    ): List<Question> {
        val limitedQuestions = when (mode) {
            QuizMode.LEGEND -> questions
            else -> questions.take(settings.questionsPerDifficulty)
        }

        val shuffledQuestions = if (settings.shuffleQuestions) {
            limitedQuestions.shuffled()
        } else {
            limitedQuestions
        }

        return if (settings.shuffleOptions) {
            shuffledQuestions.map(::shuffleQuestionOptions)
        } else {
            shuffledQuestions
        }
    }

    private fun shuffleQuestionOptions(question: Question): Question {
        val shuffledOptions = question.options.mapIndexed { index, option -> index to option }.shuffled()
        val correctAnswerIndex = shuffledOptions.indexOfFirst { it.first == question.correctAnswerIndex }
            .coerceAtLeast(0)
        return question.copy(
            options = shuffledOptions.map { it.second },
            correctAnswerIndex = correctAnswerIndex
        )
    }

    private fun calculateExpectedMaxScore(
        totalQuestions: Int,
        timerSeconds: Int,
        mode: QuizMode
    ): Int {
        var streak = 0
        var total = 0
        repeat(totalQuestions) {
            val resolution = runScoringUseCase.resolveAnswer(
                mode = mode,
                isCorrect = true,
                secondsLeft = timerSeconds,
                currentStreak = streak
            )
            total += resolution.scoreDelta
            streak = resolution.nextStreak
        }
        return total
    }

    private fun canSelectMode(mode: QuizMode, currentState: QuizUiState): Boolean {
        val selectedPackType = currentState.selectedPack?.type ?: QuizPackType.OFFICIAL_ALTAI
        return canSelectMode(
            mode = mode,
            selectedPackType = selectedPackType,
            legendUnlocked = currentState.playerProgress,
            selectedDifficulty = currentState.selectedDifficulty
        )
    }

    private fun canSelectMode(
        mode: QuizMode,
        selectedPackType: QuizPackType,
        legendUnlocked: PlayerProgress,
        selectedDifficulty: Difficulty? = Difficulty.CADET
    ): Boolean {
        if (selectedPackType == QuizPackType.CUSTOM_IMPORTED && mode != QuizMode.CLASSIC) return false
        if (mode != QuizMode.LEGEND) return true
        val difficulty = selectedDifficulty ?: Difficulty.CADET
        return legendEligibilityUseCase(legendUnlocked, difficulty)
    }

    private fun normalizeSettings(settings: QuizSettings): QuizSettings = settings.copy(
        timerSeconds = settings.timerSeconds.coerceIn(8, 60),
        autoAdvanceDelayMs = settings.autoAdvanceDelayMs.coerceAtLeast(900L),
        questionsPerDifficulty = settings.questionsPerDifficulty.coerceIn(3, 10),
        allowOptionSelection = true,
        allowFreeTextAnswers = false,
        juryModeEnabled = false,
        demoResetOnLaunch = false,
        defaultThemeId = settings.defaultThemeId.ifBlank { QuizSettings.DEFAULT_THEME_ID },
        defaultPackId = QuizPack.OFFICIAL_ALTAI_PACK_ID,
        answerMode = AnswerMode.CLASSIC_OPTIONS,
        homeContentPreference = HomeContentPreference.OFFICIAL_FIRST,
        hasCompletedOnboarding = true
    )

    private fun updateQuizSettings(transform: (QuizSettings) -> QuizSettings) {
        val updatedSettings = normalizeSettings(transform(_uiState.value.quizSettings))
        _uiState.update {
            it.copy(
                quizSettings = updatedSettings,
                timerSecondsLeft = if (!it.isAnswerLocked) updatedSettings.timerSeconds else it.timerSecondsLeft
            )
        }
        persistSettings { updatedSettings }
    }

    private fun persistSettings(transform: (QuizSettings) -> QuizSettings) {
        viewModelScope.launch {
            val updatedSettings = normalizeSettings(transform(_uiState.value.quizSettings))
            quizRepository.saveQuizSettings(updatedSettings)
        }
    }

    private fun mutateDraft(transform: (ImportedDocumentDraft) -> ImportedDocumentDraft) {
        _uiState.update { state ->
            state.copy(importedDraft = state.importedDraft?.let(transform))
        }
    }

    private fun updateDraftQuestion(
        questionId: String,
        transform: (QuestionDraft) -> QuestionDraft
    ) {
        mutateDraft { draft ->
            draft.copy(
                questionDrafts = draft.questionDrafts.map { question ->
                    if (question.id == questionId) transform(question) else question
                }
            )
        }
    }

    private fun QuestionDraft.isValid(): Boolean {
        if (text.isBlank()) return false
        if (options.size != 4) return false
        if (options.any { it.isBlank() }) return false
        return correctAnswerIndex in options.indices
    }

    private fun sanitizePackSummaries(packs: List<QuizPackSummary>): List<QuizPackSummary> {
        return packs.map { pack ->
            if (pack.type == QuizPackType.OFFICIAL_ALTAI) {
                pack.copy(
                    title = "Алтайский маршрут",
                    subtitle = "Официальный локальный набор вопросов об Алтайском крае и Барнауле."
                )
            } else {
                pack
            }
        }
    }

    private companion object {
        const val TAG = "QuizViewModel"
    }
}
