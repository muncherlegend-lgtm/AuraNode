package com.example.newapp.ui.screens.result

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.ResultMetricCard
import com.example.newapp.ui.components.ResultShareCard
import com.example.newapp.ui.quiz.QuizUiState
import com.example.newapp.ui.share.ResultShareManager
import com.example.newapp.ui.share.shareLabel

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
        ResultSharePreviewDialog(
            runSummary = runSummary,
            themePreset = uiState.selectedTheme,
            highlightFact = highlightFact,
            onDismiss = { showSharePreview = false },
            onShare = {
                ResultShareManager.shareResult(
                    context = context,
                    runSummary = runSummary,
                    themePreset = uiState.selectedTheme,
                    highlightFact = highlightFact
                ).onSuccess {
                    showSharePreview = false
                }.onFailure {
                    context.toast("Не удалось открыть системное меню отправки.")
                }
            },
            onSavePng = {
                ResultShareManager.saveResultCard(
                    context = context,
                    runSummary = runSummary,
                    themePreset = uiState.selectedTheme,
                    highlightFact = highlightFact
                ).onSuccess {
                    context.toast("PNG сохранён в изображения AuraNode.")
                }.onFailure {
                    context.toast("Не удалось сохранить PNG.")
                }
            },
            onCopyText = {
                clipboard.setText(
                    AnnotatedString(
                        ResultShareManager.buildShareText(
                            runSummary = runSummary,
                            themePreset = uiState.selectedTheme,
                            highlightFact = highlightFact
                        )
                    )
                )
                context.toast("Текст результата скопирован.")
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
                            text = resultHeadline(runSummary),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOfficialPack) {
                                "Основной маршрут по Алтайскому краю завершён. Можно открыть карту, поделиться карточкой результата или ещё раз пройти этот уровень."
                            } else {
                                "Пользовательский набор завершён. Его можно сохранить в виде карточки, отправить в мессенджер или снова пройти материал."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AuraFactChip(
                                text = runSummary?.mode?.shareLabel() ?: "Основной",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Поделиться")
                        }
                        OutlinedButton(
                            onClick = onRetryDifficulty,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(AuraNodeTestTags.RESULT_RETRY)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Пройти ещё раз")
                        }
                        if (isOfficialPack) {
                            OutlinedButton(
                                onClick = onOpenAtlas,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(AuraNodeTestTags.RESULT_OPEN_ATLAS)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Map,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
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

@Composable
private fun ResultSharePreviewDialog(
    runSummary: RunSummary,
    themePreset: ThemePreset?,
    highlightFact: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSavePng: () -> Unit,
    onCopyText: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Поделиться результатом",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Карточка уже подготовлена для мессенджеров и публикаций. Можно сразу отправить изображение, сохранить PNG или скопировать текстовую версию.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ResultShareCard(
                        runSummary = runSummary,
                        themePreset = themePreset,
                        highlightFact = highlightFact,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onShare,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Поделиться")
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val wideActions = maxWidth >= 420.dp
                        if (wideActions) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onSavePng,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Download,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(text = "Сохранить PNG")
                                }
                                OutlinedButton(
                                    onClick = onCopyText,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(text = "Копировать текст")
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onSavePng,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Download,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(text = "Сохранить PNG")
                                }
                                OutlinedButton(
                                    onClick = onCopyText,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(text = "Копировать текст")
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Закрыть")
                    }
                }
            }
        }
    }
}

private fun resultHeadline(runSummary: RunSummary?): String = when {
    runSummary == null -> "Результат пока не сформирован"
    runSummary.accuracyRatio >= 1f -> "Безошибочное прохождение"
    runSummary.accuracyRatio >= 0.75f -> "Сильный результат"
    else -> "Маршрут завершён"
}

private fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.CADET -> "Кадет"
    Difficulty.ENGINEER -> "Инженер"
    Difficulty.COSMONAUT -> "Космонавт"
}

private fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
