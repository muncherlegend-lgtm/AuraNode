package com.example.newapp.ui.screens.atlas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.atlas.AtlasNodePhotoRegistry
import com.example.newapp.ui.atlas.AtlasPanelMode
import com.example.newapp.ui.components.AtlasMapCard
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.ResultMetricCard
import com.example.newapp.ui.quiz.QuizUiState
import kotlin.math.roundToInt

@Composable
fun AtlasScreen(
    uiState: QuizUiState,
    onBackToMenu: () -> Unit,
    onSelectAtlasNode: (String) -> Unit,
    onFocusLatestUnlock: () -> Unit,
    onAtlasPanelModeChanged: (AtlasPanelMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedNode = uiState.selectedAtlasNode
    val unlockedNodeIds = uiState.unlockedAtlasNodes.map { it.id }.toSet()

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.ATLAS_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AtlasMapCard(
                nodes = uiState.atlasNodes,
                unlockedNodeIds = unlockedNodeIds,
                selectedNodeId = selectedNode?.id,
                latestUnlockedNodeId = uiState.latestUnlockedAtlasNodeId,
                focusRequestId = uiState.atlasFocusRequestId,
                motionEnabled = uiState.quizSettings.motionEnabled,
                onNodeSelected = {
                    onSelectAtlasNode(it)
                    onAtlasPanelModeChanged(AtlasPanelMode.NODE_DETAILS)
                },
                modifier = Modifier.fillMaxSize()
            )

            AuraNodeSurfaceCard(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalIconButton(onClick = onBackToMenu) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Карта Алтая",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Выберите точку на карте, чтобы открыть краткое описание и факт.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { onAtlasPanelModeChanged(AtlasPanelMode.EXPEDITION_PROGRESS) },
                            modifier = Modifier.testTag(AuraNodeTestTags.ATLAS_PANEL_PROGRESS)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.AutoGraph,
                                contentDescription = "Прогресс"
                            )
                        }
                        FilledTonalIconButton(
                            onClick = onFocusLatestUnlock,
                            modifier = Modifier.testTag(AuraNodeTestTags.ATLAS_FOCUS_LATEST)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Explore,
                                contentDescription = "К последней точке"
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { uiState.atlasCompletionRatio },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    AuraFactChip(
                        text = "Открыто ${uiState.unlockedAtlasNodes.size} из ${uiState.atlasNodes.size}",
                        compact = true
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.atlasPanelMode != AtlasPanelMode.HIDDEN,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                AuraNodeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (uiState.atlasPanelMode) {
                                    AtlasPanelMode.NODE_DETAILS -> "Точка карты"
                                    AtlasPanelMode.EXPEDITION_PROGRESS -> "Прогресс"
                                    AtlasPanelMode.HIDDEN -> ""
                                },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FilledTonalIconButton(onClick = { onAtlasPanelModeChanged(AtlasPanelMode.HIDDEN) }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        }

                        when (uiState.atlasPanelMode) {
                            AtlasPanelMode.NODE_DETAILS -> AtlasNodeDetails(
                                node = selectedNode,
                                atlasNodes = uiState.atlasNodes,
                                unlockedNodeIds = unlockedNodeIds,
                                onSelectAtlasNode = onSelectAtlasNode
                            )

                            AtlasPanelMode.EXPEDITION_PROGRESS -> AtlasProgress(
                                uiState = uiState,
                                onOpenLatest = {
                                    onFocusLatestUnlock()
                                    onAtlasPanelModeChanged(AtlasPanelMode.NODE_DETAILS)
                                }
                            )

                            AtlasPanelMode.HIDDEN -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AtlasNodeDetails(
    node: AtlasNode?,
    atlasNodes: List<AtlasNode>,
    unlockedNodeIds: Set<String>,
    onSelectAtlasNode: (String) -> Unit
) {
    if (node == null) {
        Text(
            text = "Нажмите на точку карты, чтобы открыть подробности.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val unlocked = unlockedNodeIds.contains(node.id)
    val photo = AtlasNodePhotoRegistry.photoFor(node.id)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        photo?.let {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(id = it.drawableRes),
                    contentDescription = node.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .alpha(if (unlocked) 1f else 0.45f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Фото: ${it.credit} • ${it.license}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AuraFactChip(
            text = if (unlocked) "Открыто" else "Пока закрыто",
            accent = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            compact = true
        )
        Text(
            text = node.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = node.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (unlocked) node.description else "${node.subtitle}. Точка откроется после правильных ответов в официальном наборе.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (unlocked) node.highlightFact else "Подсказка: продолжайте викторину, чтобы открыть эту точку.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (node.connections.isNotEmpty()) {
            Text(
                text = "Связанные точки",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                node.connections.forEach { connectionId ->
                    val target = atlasNodes.firstOrNull { it.id == connectionId } ?: return@forEach
                    FilterChip(
                        selected = false,
                        onClick = { onSelectAtlasNode(connectionId) },
                        label = { Text(text = target.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AtlasProgress(
    uiState: QuizUiState,
    onOpenLatest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultMetricCard(
                label = "Открыто",
                value = "${uiState.unlockedAtlasNodes.size} / ${uiState.atlasNodes.size}",
                modifier = Modifier.weight(1f),
                compact = true
            )
            ResultMetricCard(
                label = "Прогресс",
                value = "${(uiState.atlasCompletionRatio * 100).roundToInt()}%",
                modifier = Modifier.weight(1f),
                compact = true
            )
        }
        ResultMetricCard(
            label = "Достижения",
            value = uiState.earnedAchievements.size.toString(),
            compact = true
        )
        uiState.latestUnlockedAtlasNodeId?.let { latestId ->
            val latestNode = uiState.atlasNodes.firstOrNull { it.id == latestId }
            if (latestNode != null) {
                AuraNodeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Последняя открытая точка",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = latestNode.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedProgressButton(
                            text = "Показать на карте",
                            onClick = onOpenLatest
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedProgressButton(
    text: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}
