package com.example.newapp.ui.screens.result

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.ResultMetricCard
import com.example.newapp.ui.quiz.QuizUiState
import com.example.newapp.ui.share.ResultShareManager

@Composable
fun ResultScreen(
    uiState: QuizUiState,
    onBackToMenu: () -> Unit,
    onRetryDifficulty: () -> Unit,
    onOpenAtlas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val runSummary = uiState.latestRunSummary
    val highlightFact = uiState.unlockedAtlasNodes.lastOrNull()?.highlightFact
        ?: runSummary?.sourceFileName?.takeIf { it.isNotBlank() }?.let { "Материал: $it" }
        ?: "Результат сохранён локально."
    val isOfficialPack = runSummary?.packType == QuizPackType.OFFICIAL_ALTAI
    var showSharePreview by remember { mutableStateOf(false) }

    if (showSharePreview && runSummary != null) {
        AlertDialog(
            onDismissRequest = { showSharePreview = false },
            title = { Text(text = "Поделиться результатом") },
            text = {
                AuraNodeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = runSummary.packTitle.ifBlank { "AuraNode" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${runSummary.score} / ${runSummary.maxScore}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Точность ${(runSummary.accuracyRatio * 100).toInt()}% • ${difficultyLabel(runSummary.difficulty)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = highlightFact,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            ResultShareManager.shareResult(
                                context = context,
                                runSummary = runSummary,
                                themePreset = uiState.selectedTheme,
                                highlightFact = highlightFact
                            )
                            showSharePreview = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Поделиться")
                    }
                    OutlinedButton(
                        onClick = {
                            ResultShareManager.saveResultCard(
                                context = context,
                                runSummary = runSummary,
                                themePreset = uiState.selectedTheme,
                                highlightFact = highlightFact
                            )
                            showSharePreview = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Сохранить PNG")
                    }
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(
                                AnnotatedString(
                                    ResultShareManager.buildShareText(
                                        context = context,
                                        runSummary = runSummary,
                                        themePreset = uiState.selectedTheme,
                                        highlightFact = highlightFact
                                    )
                                )
                            )
                            showSharePreview = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Копировать текст")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSharePreview = false }) {
                    Text(text = "Закрыть")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.RESULT_SCREEN),
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
                        Text(
                            text = when {
                                runSummary == null -> "Результат пока не сформирован"
                                runSummary.accuracyRatio >= 1f -> "Очень точное прохождение"
                                runSummary.accuracyRatio >= 0.75f -> "Хороший результат"
                                else -> "Маршрут завершён"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOfficialPack) {
                                "Основной маршрут по Алтайскому краю завершён. Можно открыть карту или повторить этот уровень."
                            } else {
                                "Пользовательский набор завершён. При желании можно поделиться карточкой результата или снова пройти материал."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AuraFactChip(
                                text = runSummary?.mode?.let(::modeLabel) ?: "Основной",
                                compact = true
                            )
                            runSummary?.difficulty?.let {
                                AuraFactChip(
                                    text = difficultyLabel(it),
                                    accent = MaterialTheme.colorScheme.secondary,
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }

            if (runSummary == null) {
                item {
                    OutlinedButton(
                        onClick = onBackToMenu,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "В главное меню")
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultMetricCard(
                            label = "Очки",
                            value = "${runSummary.score} / ${runSummary.maxScore}",
                            modifier = Modifier.weight(1f)
                        )
                        ResultMetricCard(
                            label = "Точность",
                            value = "${(runSummary.accuracyRatio * 100).toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultMetricCard(
                            label = "Верных ответов",
                            value = "${runSummary.correctAnswers} из ${runSummary.totalQuestions}",
                            modifier = Modifier.weight(1f),
                            compact = true
                        )
                        ResultMetricCard(
                            label = "Лучшая серия",
                            value = runSummary.longestStreak.toString(),
                            modifier = Modifier.weight(1f),
                            compact = true
                        )
                    }
                }

                item {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isOfficialPack) "Основной маршрут" else "Пользовательский набор",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = runSummary.packTitle.ifBlank { "AuraNode" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (runSummary.sourceFileName.isNotBlank()) {
                                Text(
                                    text = "Источник: ${runSummary.sourceFileName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = highlightFact,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showSharePreview = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Поделиться")
                        }
                        Button(
                            onClick = onRetryDifficulty,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(AuraNodeTestTags.RESULT_RETRY)
                        ) {
                            Text(text = "Пройти ещё раз")
                        }
                        if (isOfficialPack) {
                            OutlinedButton(
                                onClick = onOpenAtlas,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(AuraNodeTestTags.RESULT_OPEN_ATLAS)
                            ) {
                                Text(text = "Открыть карту")
                            }
                        }
                        OutlinedButton(
                            onClick = onBackToMenu,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "В главное меню")
                        }
                    }
                }
            }
        }
    }
}

private fun difficultyLabel(difficulty: com.example.newapp.data.model.Difficulty): String = when (difficulty) {
    com.example.newapp.data.model.Difficulty.CADET -> "Кадет"
    com.example.newapp.data.model.Difficulty.ENGINEER -> "Инженер"
    com.example.newapp.data.model.Difficulty.COSMONAUT -> "Космонавт"
}

private fun modeLabel(mode: com.example.newapp.data.model.QuizMode): String = when (mode) {
    com.example.newapp.data.model.QuizMode.CLASSIC -> "Основной"
    com.example.newapp.data.model.QuizMode.SPRINT -> "Быстрый"
    com.example.newapp.data.model.QuizMode.LEGEND -> "Углублённый"
}
