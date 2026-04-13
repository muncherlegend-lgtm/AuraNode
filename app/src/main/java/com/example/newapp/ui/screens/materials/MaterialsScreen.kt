package com.example.newapp.ui.screens.materials

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuestionDraft
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.QuizPackCard
import com.example.newapp.ui.quiz.QuizUiState

@Composable
fun MaterialsScreen(
    uiState: QuizUiState,
    onBack: () -> Unit,
    onImportDocumentSelected: (Uri) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onStartPackQuiz: (String, Difficulty) -> Unit,
    onDeletePack: (String) -> Unit,
    onDeleteAllCustomPacks: () -> Unit,
    onDismissMessage: () -> Unit,
    onClearDraft: () -> Unit,
    onRebuildDraftQuestions: () -> Unit,
    onDraftTitleChanged: (String) -> Unit,
    onDraftDescriptionChanged: (String) -> Unit,
    onToggleSection: (String) -> Unit,
    onAddDraftQuestion: () -> Unit,
    onRemoveDraftQuestion: (String) -> Unit,
    onDraftQuestionTextChanged: (String, String) -> Unit,
    onDraftQuestionOptionChanged: (String, Int, String) -> Unit,
    onDraftQuestionCorrectAnswerChanged: (String, Int) -> Unit,
    onDraftQuestionDifficultyChanged: (String, Difficulty) -> Unit,
    onDraftQuestionExplanationChanged: (String, String) -> Unit,
    onSaveDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDifficulty = uiState.selectedDifficulty ?: Difficulty.CADET
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportDocumentSelected(uri)
    }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.MATERIALS_SCREEN),
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
                            FilledTonalIconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Материалы",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Импортируйте документ, соберите черновик и сохраните свой набор вопросов локально.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                importLauncher.launch(
                                    arrayOf(
                                        "text/plain",
                                        "text/markdown",
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Импортировать файл")
                        }

                        AuraFactChip(
                            text = "Поддерживаются TXT, MD, PDF, DOCX",
                            compact = true
                        )
                    }
                }
            }

            uiState.generationErrorMessage?.let { message ->
                item {
                    AuraNodeSurfaceCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(onClick = onDismissMessage) {
                                Text(text = "Скрыть")
                            }
                        }
                    }
                }
            }

            item {
                MaterialsSection(title = "Запуск сохранённых наборов") {
                    Text(
                        text = "Уровень запуска",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { difficulty ->
                            FilterChip(
                                selected = selectedDifficulty == difficulty,
                                onClick = { onDifficultySelected(difficulty) },
                                label = { Text(text = difficulty.label()) }
                            )
                        }
                    }
                    if (uiState.customPacks.isEmpty()) {
                        Text(
                            text = "Пока нет пользовательских наборов. После импорта здесь появится библиотека материалов.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.customPacks.forEach { pack ->
                                QuizPackCard(
                                    pack = pack,
                                    selected = uiState.selectedPackId == pack.id,
                                    onClick = { onStartPackQuiz(pack.id, selectedDifficulty) },
                                    onDelete = { onDeletePack(pack.id) }
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onDeleteAllCustomPacks,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Очистить все пользовательские наборы")
                        }
                    }
                }
            }

            uiState.importedDraft?.let { draft ->
                item {
                    MaterialsSection(title = "Черновик набора") {
                        OutlinedTextField(
                            value = draft.title,
                            onValueChange = onDraftTitleChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Название") }
                        )
                        OutlinedTextField(
                            value = draft.description,
                            onValueChange = onDraftDescriptionChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Описание") },
                            minLines = 2
                        )
                        AuraFactChip(
                            text = "${draft.document.displayName} • ${draft.document.sourceExtension.uppercase()}",
                            compact = true
                        )
                        Text(
                            text = draft.document.previewExcerpt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                item {
                    MaterialsSection(title = "Разделы") {
                        Text(
                            text = "Отключите ненужные фрагменты и обновите черновик вопросов только по выбранным разделам.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        draft.sections.forEach { section ->
                            AuraNodeSurfaceCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = section.included,
                                        onCheckedChange = { onToggleSection(section.id) }
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = section.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = section.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onRebuildDraftQuestions,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Обновить вопросы")
                            }
                            OutlinedButton(
                                onClick = onClearDraft,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Закрыть черновик")
                            }
                        }
                    }
                }

                item {
                    MaterialsSection(title = "Вопросы") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            draft.questionDrafts.forEach { question ->
                                DraftQuestionCard(
                                    question = question,
                                    onTextChanged = { onDraftQuestionTextChanged(question.id, it) },
                                    onOptionChanged = { index, value ->
                                        onDraftQuestionOptionChanged(question.id, index, value)
                                    },
                                    onCorrectAnswerChanged = { onDraftQuestionCorrectAnswerChanged(question.id, it) },
                                    onDifficultyChanged = { onDraftQuestionDifficultyChanged(question.id, it) },
                                    onExplanationChanged = { onDraftQuestionExplanationChanged(question.id, it) },
                                    onRemove = { onRemoveDraftQuestion(question.id) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onAddDraftQuestion,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Добавить вопрос")
                            }
                            Button(
                                onClick = onSaveDraft,
                                enabled = !uiState.isGeneratingPack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (uiState.isGeneratingPack) "Сохраняем…" else "Сохранить набор")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialsSection(
    title: String,
    content: @Composable () -> Unit
) {
    AuraNodeSurfaceCard {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun DraftQuestionCard(
    question: QuestionDraft,
    onTextChanged: (String) -> Unit,
    onOptionChanged: (Int, String) -> Unit,
    onCorrectAnswerChanged: (Int) -> Unit,
    onDifficultyChanged: (Difficulty) -> Unit,
    onExplanationChanged: (String) -> Unit,
    onRemove: () -> Unit
) {
    AuraNodeSurfaceCard {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = question.text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Текст вопроса") }
            )
            question.options.forEachIndexed { index, option ->
                OutlinedTextField(
                    value = option,
                    onValueChange = { onOptionChanged(index, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Вариант ${index + 1}") }
                )
            }
            Text(
                text = "Правильный вариант",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { index ->
                    FilterChip(
                        selected = question.correctAnswerIndex == index,
                        onClick = { onCorrectAnswerChanged(index) },
                        label = { Text(text = "${index + 1}") }
                    )
                }
            }
            Text(
                text = "Уровень сложности",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { difficulty ->
                    FilterChip(
                        selected = question.difficulty == difficulty,
                        onClick = { onDifficultyChanged(difficulty) },
                        label = { Text(text = difficulty.label()) }
                    )
                }
            }
            OutlinedTextField(
                value = question.explanation,
                onValueChange = onExplanationChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Пояснение") },
                minLines = 2
            )
            OutlinedButton(
                onClick = onRemove,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Удалить вопрос")
            }
        }
    }
}

private fun Difficulty.label(): String = when (this) {
    Difficulty.CADET -> "Кадет"
    Difficulty.ENGINEER -> "Инженер"
    Difficulty.COSMONAUT -> "Космонавт"
}
