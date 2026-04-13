package com.example.newapp.ui.screens.settings

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
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.quiz.QuizUiState

@Composable
fun SettingsScreen(
    uiState: QuizUiState,
    onBack: () -> Unit,
    onOpenThemes: () -> Unit,
    onTimerEnabledChanged: (Boolean) -> Unit,
    onTimerSecondsChanged: (Int) -> Unit,
    onQuestionsCountChanged: (Int) -> Unit,
    onShuffleQuestionsChanged: (Boolean) -> Unit,
    onShuffleOptionsChanged: (Boolean) -> Unit,
    onCompactUiChanged: (Boolean) -> Unit,
    onMotionChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onDeleteAllCustomPacks: () -> Unit,
    onClearProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.quizSettings

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.SETTINGS_SCREEN),
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
                                    text = "Настройки",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Параметры прохождения, интерфейса и локальных данных приложения.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenThemes,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Открыть темы")
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "Прохождение") {
                    StepperRow(
                        title = "Секунд на вопрос",
                        value = settings.timerSeconds.toString(),
                        onDecrement = { onTimerSecondsChanged(settings.timerSeconds - 1) },
                        onIncrement = { onTimerSecondsChanged(settings.timerSeconds + 1) }
                    )
                    ToggleRow(
                        title = "Показывать таймер",
                        body = "Если выключено, вопрос остаётся без обратного отсчёта.",
                        checked = settings.showTimer,
                        onCheckedChange = onTimerEnabledChanged
                    )
                    StepperRow(
                        title = "Вопросов на уровень",
                        value = settings.questionsPerDifficulty.toString(),
                        onDecrement = { onQuestionsCountChanged(settings.questionsPerDifficulty - 1) },
                        onIncrement = { onQuestionsCountChanged(settings.questionsPerDifficulty + 1) }
                    )
                    ToggleRow(
                        title = "Перемешивать вопросы",
                        body = "Меняет порядок вопросов внутри выбранного уровня.",
                        checked = settings.shuffleQuestions,
                        onCheckedChange = onShuffleQuestionsChanged
                    )
                    ToggleRow(
                        title = "Перемешивать варианты",
                        body = "Меняет порядок четырёх вариантов ответа.",
                        checked = settings.shuffleOptions,
                        onCheckedChange = onShuffleOptionsChanged
                    )
                }
            }

            item {
                SettingsSection(title = "Интерфейс") {
                    ToggleRow(
                        title = "Компактный интерфейс",
                        body = "Уменьшает отступы и помогает на небольших экранах.",
                        checked = settings.compactUi,
                        onCheckedChange = onCompactUiChanged
                    )
                    ToggleRow(
                        title = "Анимации",
                        body = "Плавные переходы и мягкие появления карточек.",
                        checked = settings.motionEnabled,
                        onCheckedChange = onMotionChanged
                    )
                    ToggleRow(
                        title = "Тактильный отклик",
                        body = "Короткая вибрация на действиях и ответах.",
                        checked = settings.hapticsEnabled,
                        onCheckedChange = onHapticsChanged
                    )
                    ToggleRow(
                        title = "Звук",
                        body = "Локальные звуковые подтверждения интерфейса.",
                        checked = settings.soundEnabled,
                        onCheckedChange = onSoundChanged
                    )
                }
            }

            item {
                SettingsSection(title = "Материалы") {
                    Text(
                        text = "Импорт работает локально: TXT, MD, PDF и DOCX. Количество вопросов на уровень берётся из параметра выше.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SettingsSection(title = "Данные") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDeleteAllCustomPacks,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Очистить материалы")
                        }
                        OutlinedButton(
                            onClick = onClearProgress,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Сбросить результаты")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
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
private fun ToggleRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDecrement) {
                Text(text = "−")
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(onClick = onIncrement) {
                Text(text = "+")
            }
        }
    }
}
