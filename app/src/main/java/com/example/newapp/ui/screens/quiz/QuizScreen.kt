package com.example.newapp.ui.screens.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.quiz.AnswerFeedbackType
import com.example.newapp.ui.quiz.QuizUiState
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuestion = uiState.currentQuestion
    if (currentQuestion == null) {
        Scaffold(
            modifier = modifier.testTag(AuraNodeTestTags.QUIZ_SCREEN),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Вопросы ещё не загружены.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.QUIZ_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AuraNodeSurfaceCard(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalIconButton(onClick = onReturnToMenu) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.selectedPack?.title ?: "Викторина",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.currentQuestionIndex + 1} из ${uiState.totalQuestions}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AuraFactChip(
                                text = difficultyLabel(uiState.selectedDifficulty ?: Difficulty.CADET),
                                compact = true
                            )
                        }

                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AuraFactChip(
                                text = "Режим: ${modeLabel(uiState.selectedMode)}",
                                compact = true
                            )
                            if (uiState.quizSettings.showTimer) {
                                AuraFactChip(
                                    text = "Таймер: ${uiState.timerSecondsLeft} с",
                                    accent = MaterialTheme.colorScheme.secondary,
                                    compact = true
                                )
                            }
                            AuraFactChip(
                                text = "Очки: ${uiState.score}",
                                accent = MaterialTheme.colorScheme.tertiary,
                                compact = true
                            )
                        }
                    }
                }
            }

            item {
                AnimatedContent(
                    targetState = uiState.currentQuestionIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "question"
                ) {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currentQuestion.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Выберите один из четырёх вариантов ответа.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        AnswerOptionCard(
                            text = option,
                            state = optionState(uiState, index),
                            enabled = !uiState.isAnswerLocked,
                            onClick = { onAnswerSelected(index) },
                            modifier = Modifier.testTag(AuraNodeTestTags.answerOptionTag(index))
                        )
                    }
                }
            }

            if (uiState.answerFeedbackType != null) {
                item {
                    AuraNodeSurfaceCard(
                        colors = CardDefaults.cardColors(
                            containerColor = when (uiState.answerFeedbackType) {
                                AnswerFeedbackType.CORRECT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                                AnswerFeedbackType.INCORRECT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.38f)
                                AnswerFeedbackType.TIMEOUT -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (uiState.answerFeedbackType) {
                                    AnswerFeedbackType.CORRECT -> "Верный ответ"
                                    AnswerFeedbackType.INCORRECT -> "Ответ можно уточнить"
                                    AnswerFeedbackType.TIMEOUT -> "Время вышло"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = buildFeedbackText(uiState),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onReturnToMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "В главное меню")
                }
            }
        }
    }
}

@Composable
private fun AnswerOptionCard(
    text: String,
    state: OptionVisualState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (state) {
        OptionVisualState.Default -> MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        OptionVisualState.Correct -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f)
        OptionVisualState.Wrong -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.52f)
        OptionVisualState.Disabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    }
    val borderColor = when (state) {
        OptionVisualState.Default -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        OptionVisualState.Correct -> MaterialTheme.colorScheme.primary
        OptionVisualState.Wrong -> MaterialTheme.colorScheme.error
        OptionVisualState.Disabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    AuraNodeSurfaceCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        borderColor = borderColor
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun optionState(uiState: QuizUiState, optionIndex: Int): OptionVisualState {
    if (!uiState.isAnswerLocked) return OptionVisualState.Default
    return when {
        optionIndex == uiState.revealedAnswerIndex -> OptionVisualState.Correct
        optionIndex == uiState.selectedAnswerIndex -> OptionVisualState.Wrong
        else -> OptionVisualState.Disabled
    }
}

private fun buildFeedbackText(uiState: QuizUiState): String {
    val question = uiState.currentQuestion ?: return ""
    val correctAnswer = question.options.getOrNull(question.correctAnswerIndex).orEmpty()
    return when (uiState.answerFeedbackType) {
        AnswerFeedbackType.CORRECT -> question.explanation
        AnswerFeedbackType.INCORRECT -> "Верный ответ: $correctAnswer. ${question.explanation}"
        AnswerFeedbackType.TIMEOUT -> "Время закончилось. Верный ответ: $correctAnswer. ${question.explanation}"
        null -> ""
    }
}

private fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.CADET -> "Кадет"
    Difficulty.ENGINEER -> "Инженер"
    Difficulty.COSMONAUT -> "Космонавт"
}

private fun modeLabel(mode: QuizMode): String = when (mode) {
    QuizMode.CLASSIC -> "Основной"
    QuizMode.SPRINT -> "Быстрый"
    QuizMode.LEGEND -> "Углублённый"
}

private enum class OptionVisualState {
    Default,
    Correct,
    Wrong,
    Disabled
}
