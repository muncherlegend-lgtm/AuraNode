package com.example.newapp.ui.screens.quiz

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.newapp.R
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AnswerOptionCard
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.FactRevealCard
import com.example.newapp.ui.components.QuizTopChrome
import com.example.newapp.ui.components.toPresentation
import com.example.newapp.ui.quiz.AnswerFeedbackType
import com.example.newapp.ui.quiz.QuizUiState

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onAnswerInputChanged: (String) -> Unit,
    onSubmitTypedAnswer: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState.currentQuestion
    val settings = uiState.quizSettings
    val compact = settings.compactUi
    val difficultyPresentation = uiState.selectedDifficulty?.toPresentation()
    val haptic = LocalHapticFeedback.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }

    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }

    LaunchedEffect(uiState.answerFeedbackType) {
        when (uiState.answerFeedbackType) {
            AnswerFeedbackType.CORRECT -> {
                if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (settings.soundEnabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            }

            AnswerFeedbackType.INCORRECT, AnswerFeedbackType.TIMEOUT -> {
                if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (settings.soundEnabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 140)
            }

            null -> Unit
        }
    }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.QUIZ_SCREEN),
        containerColor = Color.Transparent,
        topBar = {
            if (question != null) {
                QuizTopChrome(
                    routeTitle = stringResource(R.string.quiz_topbar_route),
                    levelLabel = stringResource(
                        difficultyPresentation?.titleRes ?: R.string.quiz_title_fallback
                    ),
                    questionLabel = stringResource(
                        R.string.quiz_topbar_question,
                        uiState.currentQuestionIndex + 1
                    ),
                    questionCounter = stringResource(
                        R.string.quiz_topbar_counter,
                        uiState.currentQuestionIndex + 1,
                        uiState.totalQuestions
                    ),
                    progress = uiState.progress,
                    timerLabel = if (settings.showTimer) {
                        stringResource(R.string.quiz_timer_value, uiState.timerSecondsLeft)
                    } else {
                        null
                    },
                    onMenuClick = onReturnToMenu,
                    compact = compact
                )
            }
        }
    ) { innerPadding ->
        if (question == null) {
            EmptyQuizState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                isQuizCompleted = uiState.isQuizCompleted,
                onReturnToMenu = onReturnToMenu
            )
            return@Scaffold
        }

        val feedbackType = uiState.answerFeedbackType
        val feedbackTitle = when (feedbackType) {
            AnswerFeedbackType.CORRECT -> stringResource(R.string.quiz_feedback_correct_title)
            AnswerFeedbackType.INCORRECT -> stringResource(R.string.quiz_feedback_incorrect_title)
            AnswerFeedbackType.TIMEOUT -> stringResource(R.string.quiz_feedback_timeout_title)
            null -> null
        }
        val feedbackBody = when (feedbackType) {
            AnswerFeedbackType.CORRECT -> stringResource(
                R.string.quiz_feedback_correct_reward_body,
                question.unlockReward.ifBlank { stringResource(R.string.quiz_feedback_fallback_reward) },
                question.explanation
            )

            AnswerFeedbackType.INCORRECT -> {
                val submittedText = uiState.submittedAnswerText
                if (submittedText != null && uiState.selectedAnswerIndex == null) {
                    stringResource(
                        R.string.quiz_feedback_incorrect_with_input_body,
                        submittedText,
                        question.options[question.correctAnswerIndex],
                        question.explanation
                    )
                } else {
                    stringResource(
                        R.string.quiz_feedback_incorrect_body,
                        question.options[question.correctAnswerIndex],
                        question.explanation
                    )
                }
            }

            AnswerFeedbackType.TIMEOUT -> stringResource(
                R.string.quiz_feedback_timeout_body,
                question.options[question.correctAnswerIndex],
                question.explanation
            )

            null -> null
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuraFactChip(
                        text = stringResource(
                            R.string.quiz_mode_chip,
                            uiState.selectedMode.name
                        ),
                        accent = MaterialTheme.colorScheme.primary,
                        compact = true
                    )
                    uiState.selectedPack?.let { pack ->
                        AuraFactChip(
                            text = pack.title,
                            accent = MaterialTheme.colorScheme.tertiary,
                            compact = true
                        )
                    }
                    AuraFactChip(
                        text = stringResource(
                            R.string.quiz_score_value,
                            stringResource(R.string.quiz_score_label),
                            uiState.score
                        ),
                        accent = MaterialTheme.colorScheme.secondary,
                        compact = true
                    )
                    if (uiState.selectedMode != com.example.newapp.data.model.QuizMode.CLASSIC) {
                        AuraFactChip(
                            text = stringResource(
                                R.string.quiz_streak_and_bonus,
                                uiState.longestStreak,
                                uiState.timeBonus
                            ),
                            accent = MaterialTheme.colorScheme.tertiary,
                            compact = true
                        )
                    }
                }
            }

            item {
                if (settings.motionEnabled) {
                    AnimatedContent(
                        targetState = question,
                        label = "questionMotion",
                        transitionSpec = {
                            (fadeIn() + slideInVertically(initialOffsetY = { it / 7 })) togetherWith
                                (fadeOut() + slideOutVertically(targetOffsetY = { -it / 9 }))
                        }
                    ) { targetQuestion ->
                        QuestionCard(
                            text = targetQuestion.text,
                            compact = compact
                        )
                    }
                } else {
                    QuestionCard(
                        text = question.text,
                        compact = compact
                    )
                }
            }

            if (settings.answerMode != AnswerMode.CLASSIC_OPTIONS) {
                item {
                    TextAnswerCard(
                        value = uiState.answerInput,
                        onValueChange = onAnswerInputChanged,
                        onSubmit = onSubmitTypedAnswer,
                        enabled = !uiState.isAnswerLocked,
                        answerMode = settings.answerMode,
                        compact = compact
                    )
                }
            }

            if (settings.answerMode != AnswerMode.EXPLORER_TEXT) {
                items(question.options.indices.toList()) { index ->
                    AnswerOptionCard(
                        optionIndex = index,
                        optionText = question.options[index],
                        selectedAnswerIndex = uiState.selectedAnswerIndex,
                        revealedAnswerIndex = uiState.revealedAnswerIndex,
                        isAnswerLocked = uiState.isAnswerLocked,
                        onClick = { onAnswerSelected(index) },
                        modifier = Modifier.testTag(AuraNodeTestTags.answerOptionTag(index)),
                        compact = compact
                    )
                }
            }

            item {
                if (feedbackType != null && feedbackTitle != null && feedbackBody != null) {
                    FactRevealCard(
                        visible = true,
                        title = feedbackTitle,
                        body = feedbackBody,
                        type = feedbackType,
                        compact = compact
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    text: String,
    compact: Boolean
) {
    AuraNodeSurfaceCard {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 18.dp else 22.dp,
                vertical = if (compact) 16.dp else 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.quiz_progress_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.quiz_fact_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextAnswerCard(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    answerMode: AnswerMode,
    compact: Boolean
) {
    AuraNodeSurfaceCard {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 16.dp else 18.dp,
                vertical = if (compact) 14.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.quiz_answer_input_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (answerMode == AnswerMode.EXPLORER_MIXED) {
                    stringResource(R.string.quiz_answer_input_hint_dual)
                } else {
                    stringResource(R.string.quiz_answer_input_hint_text_only)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AuraNodeTestTags.QUIZ_TEXT_INPUT),
                enabled = enabled,
                singleLine = true,
                label = { Text(text = stringResource(R.string.quiz_answer_input_label)) },
                placeholder = { Text(text = stringResource(R.string.quiz_answer_input_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() })
            )
            Button(
                onClick = onSubmit,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AuraNodeTestTags.QUIZ_TEXT_SUBMIT)
            ) {
                Text(text = stringResource(R.string.quiz_answer_input_submit))
            }
        }
    }
}

@Composable
private fun EmptyQuizState(
    modifier: Modifier = Modifier,
    isQuizCompleted: Boolean,
    onReturnToMenu: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isQuizCompleted) {
                stringResource(R.string.quiz_empty_loading)
            } else {
                stringResource(R.string.quiz_empty_missing)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!isQuizCompleted) {
            Button(
                onClick = onReturnToMenu,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = stringResource(R.string.common_back_to_menu))
            }
        }
    }
}
