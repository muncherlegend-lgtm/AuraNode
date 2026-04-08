package com.example.newapp.ui.screens.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.RunSummary
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AchievementChip
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.ResultMetricCard
import com.example.newapp.ui.components.ResultShareCard
import com.example.newapp.ui.quiz.QuizUiState
import kotlin.math.roundToInt

@Composable
fun ResultScreen(
    uiState: QuizUiState,
    onBackToMenu: () -> Unit,
    onRetryDifficulty: () -> Unit,
    onOpenAtlas: () -> Unit,
    onShareResult: (RunSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val compact = uiState.quizSettings.compactUi
    val runSummary = uiState.latestRunSummary
    val isOfficialPack = runSummary?.packType == QuizPackType.OFFICIAL_ALTAI

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.RESULT_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (runSummary == null) {
            EmptyResultState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onBackToMenu = onBackToMenu
            )
            return@Scaffold
        }

        var reveal by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { reveal = true }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = reveal,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
                ) {
                    AuraNodeSurfaceCard(
                        modifier = Modifier.statusBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = if (compact) 18.dp else 22.dp,
                                vertical = if (compact) 18.dp else 22.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AuraFactChip(
                                text = stringResource(R.string.result_summary_label),
                                compact = true
                            )
                            Text(
                                text = stringResource(R.string.result_debrief_title),
                                style = if (compact) {
                                    MaterialTheme.typography.headlineSmall
                                } else {
                                    MaterialTheme.typography.displaySmall
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (runSummary.packTitle.isNotBlank()) {
                                    AuraFactChip(
                                        text = runSummary.packTitle,
                                        accent = MaterialTheme.colorScheme.primary,
                                        compact = true
                                    )
                                }
                                AuraFactChip(
                                    text = runSummary.mode.name,
                                    accent = MaterialTheme.colorScheme.primaryContainer,
                                    compact = true
                                )
                                AuraFactChip(
                                    text = runSummary.medalTier.name,
                                    accent = MaterialTheme.colorScheme.secondary,
                                    compact = true
                                )
                                if (!isOfficialPack) {
                                    AuraFactChip(
                                        text = when (runSummary.packGenerationSource) {
                                            PackGenerationSource.OFFICIAL -> "Official"
                                            PackGenerationSource.CLOUD_AI -> "AI"
                                            PackGenerationSource.OFFLINE_DRAFT -> "Offline Draft"
                                        },
                                        accent = MaterialTheme.colorScheme.tertiary,
                                        compact = true
                                    )
                                }
                                uiState.selectedTheme?.let { preset ->
                                    AuraFactChip(
                                        text = preset.title,
                                        accent = MaterialTheme.colorScheme.tertiary,
                                        compact = true
                                    )
                                }
                            }
                            Text(
                                text = stringResource(
                                    R.string.result_score_format,
                                    runSummary.score,
                                    runSummary.maxScore
                                ),
                                style = if (compact) {
                                    MaterialTheme.typography.headlineLarge
                                } else {
                                    MaterialTheme.typography.displayLarge
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOfficialPack) {
                                    stringResource(
                                        R.string.result_debrief_body,
                                        uiState.unlockedAtlasNodes.size
                                    )
                                } else {
                                    stringResource(
                                        R.string.result_custom_debrief_body,
                                        runSummary.sourceFileName.ifBlank { runSummary.packTitle }
                                    )
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultMetricCard(
                        label = stringResource(R.string.result_metric_accuracy),
                        value = stringResource(
                            R.string.result_accuracy_format,
                            (runSummary.accuracyRatio * 100).roundToInt()
                        ),
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                    ResultMetricCard(
                        label = stringResource(R.string.result_metric_correct),
                        value = stringResource(
                            R.string.result_correct_format,
                            runSummary.correctAnswers,
                            runSummary.totalQuestions
                        ),
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultMetricCard(
                        label = stringResource(R.string.result_metric_streak),
                        value = runSummary.longestStreak.toString(),
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                    ResultMetricCard(
                        label = stringResource(R.string.result_metric_time_bonus),
                        value = runSummary.timeBonus.toString(),
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }
            }

            item {
                ResultShareCard(
                    runSummary = runSummary,
                    themePreset = uiState.selectedTheme,
                    highlightFact = if (isOfficialPack) {
                        uiState.unlockedAtlasNodes.lastOrNull()?.highlightFact
                            ?: stringResource(R.string.result_share_fallback_fact)
                    } else {
                        stringResource(
                            R.string.result_custom_share_fact,
                            runSummary.sourceFileName.ifBlank { runSummary.packTitle }
                        )
                    }
                )
            }

            if (!isOfficialPack) {
                item {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.result_custom_pack_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.result_custom_pack_body,
                                    runSummary.sourceFileName.ifBlank { runSummary.packTitle }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isOfficialPack && uiState.unlockedAtlasNodes.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.result_unlocked_nodes_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(uiState.unlockedAtlasNodes.takeLast(3), key = { it.id }) { node ->
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = node.rewardTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = node.highlightFact,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (uiState.earnedAchievements.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.result_achievements_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.earnedAchievements.take(3).forEach { achievement ->
                            AchievementChip(achievement = achievement)
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onShareResult(runSummary) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.result_share))
                    }
                    if (isOfficialPack) {
                        Button(
                            onClick = onOpenAtlas,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(AuraNodeTestTags.RESULT_OPEN_ATLAS)
                        ) {
                            Text(text = stringResource(R.string.result_open_atlas))
                        }
                    }
                    OutlinedButton(
                        onClick = onRetryDifficulty,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(AuraNodeTestTags.RESULT_RETRY)
                    ) {
                        Text(text = stringResource(R.string.result_retry))
                    }
                    OutlinedButton(
                        onClick = onBackToMenu,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.result_back_to_menu))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultState(
    modifier: Modifier = Modifier,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.result_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = onBackToMenu,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.result_back_to_menu))
        }
    }
}
