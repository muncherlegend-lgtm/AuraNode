package com.example.newapp.ui.screens.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.DifficultyRouteCard
import com.example.newapp.ui.components.QuizModeCard
import com.example.newapp.ui.quiz.QuizUiState

@Composable
fun MenuScreen(
    uiState: QuizUiState,
    onStartQuiz: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onModeSelected: (QuizMode) -> Unit,
    onOpenAtlas: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        LoadingMenuState(modifier = modifier)
        return
    }

    val selectedDifficulty = uiState.selectedDifficulty ?: Difficulty.CADET
    val selectedPack = uiState.selectedPack

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.MENU_SCREEN),
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
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AuraFactChip(
                            text = if (uiState.isOfficialPackSelected) {
                                "Алтайский край • Барнаул"
                            } else {
                                "Пользовательский материал"
                            },
                            compact = true
                        )

                        Text(
                            text = "AuraNode",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (uiState.isOfficialPackSelected) {
                                "Спокойная учебная викторина о регионе: выберите режим, уровень и начните прохождение без лишних экранов."
                            } else {
                                "Выбран пользовательский набор. Его можно запустить отсюда или открыть раздел «Материалы» для редактирования."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (selectedPack != null) {
                            AuraNodeSurfaceCard(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
                                ),
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = selectedPack.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedPack.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onStartQuiz,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("menu_start_quiz")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Начать")
                        }

                        SectionBlock(
                            title = "Уровень",
                            body = "Три уровня сложности с разным темпом восприятия материала."
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(Difficulty.entries) { difficulty ->
                                    DifficultyRouteCard(
                                        difficulty = difficulty,
                                        selected = selectedDifficulty == difficulty,
                                        onClick = { onDifficultySelected(difficulty) },
                                        compact = true,
                                        modifier = Modifier
                                            .width(232.dp)
                                            .testTag(AuraNodeTestTags.difficultyTag(difficulty))
                                    )
                                }
                            }
                        }

                        SectionBlock(
                            title = "Режим",
                            body = "Основной маршрут всегда остаётся главным сценарием, а дополнительные режимы подаются спокойнее."
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(QuizMode.entries) { mode ->
                                    val enabled = when {
                                        !uiState.isOfficialPackSelected && mode != QuizMode.CLASSIC -> false
                                        mode == QuizMode.LEGEND && !uiState.isLegendAvailable -> false
                                        else -> true
                                    }
                                    QuizModeCard(
                                        mode = mode,
                                        selected = uiState.selectedMode == mode,
                                        enabled = enabled,
                                        onClick = { if (enabled) onModeSelected(mode) },
                                        modifier = Modifier
                                            .width(260.dp)
                                            .testTag(AuraNodeTestTags.modeTag(mode))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Разделы",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    SecondaryActionCard(
                        title = "Карта Алтая",
                        body = "Точки региона, прогресс маршрута и фотографии объектов без перегружающих панелей.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenAtlas,
                        modifier = Modifier.testTag(AuraNodeTestTags.MENU_OPEN_ATLAS)
                    )

                    SecondaryActionCard(
                        title = "Материалы",
                        body = "Импортируйте TXT, MD, PDF или DOCX, соберите черновик и сохраните свой набор вопросов.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Source,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        onClick = onOpenMaterials,
                        modifier = Modifier.testTag(AuraNodeTestTags.MENU_IMPORT_MATERIAL)
                    )

                    SecondaryActionCard(
                        title = "Настройки",
                        body = "Таймер, количество вопросов, анимации, звук и локальные данные приложения.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag(AuraNodeTestTags.MENU_OPEN_SETTINGS)
                    )

                    SecondaryActionCard(
                        title = "Темы",
                        body = "Отдельный экран с палитрами и фоновыми сценами для карты, викторины и карточки результата.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenThemes,
                        modifier = Modifier.testTag(AuraNodeTestTags.MENU_OPEN_THEMES)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    body: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun SecondaryActionCard(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onClick) {
                Text(text = "Открыть")
            }
        }
    }
}

@Composable
private fun LoadingMenuState(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.MENU_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Подготавливаем приложение…",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Загружаем темы, карту и локальные материалы.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
