package com.example.newapp.ui.screens.atlas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.FactCategory
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.atlas.AtlasPanelMode
import com.example.newapp.ui.components.AchievementChip
import com.example.newapp.ui.components.AtlasMapCard
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.HallOfFameRunCard
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
    val selectedNode = uiState.selectedAtlasNode ?: uiState.atlasNodes.firstOrNull()
    val unlockedNodeIds = uiState.unlockedAtlasNodes.map { it.id }.toSet()
    val compact = uiState.quizSettings.compactUi

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

            AtlasTopChrome(
                uiState = uiState,
                compact = compact,
                onBackToMenu = onBackToMenu,
                onFocusLatestUnlock = onFocusLatestUnlock,
                onShowProgress = { onAtlasPanelModeChanged(AtlasPanelMode.EXPEDITION_PROGRESS) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter)
            )

            AtlasBottomPanel(
                uiState = uiState,
                selectedNode = selectedNode,
                compact = compact,
                unlockedNodeIds = unlockedNodeIds,
                onSelectAtlasNode = onSelectAtlasNode,
                onAtlasPanelModeChanged = onAtlasPanelModeChanged,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun AtlasTopChrome(
    uiState: QuizUiState,
    compact: Boolean,
    onBackToMenu: () -> Unit,
    onFocusLatestUnlock: () -> Unit,
    onShowProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(onClick = onBackToMenu) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.atlas_back_to_menu)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.atlas_title),
                        style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.atlas_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = onShowProgress,
                    modifier = Modifier.testTag(AuraNodeTestTags.ATLAS_PANEL_PROGRESS)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoGraph,
                        contentDescription = stringResource(R.string.atlas_panel_progress)
                    )
                }

                FilledTonalIconButton(
                    onClick = onFocusLatestUnlock,
                    enabled = uiState.latestUnlockedAtlasNodeId != null,
                    modifier = Modifier.testTag(AuraNodeTestTags.ATLAS_FOCUS_LATEST)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Explore,
                        contentDescription = stringResource(R.string.atlas_focus_latest)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { uiState.atlasCompletionRatio },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuraFactChip(
                    text = stringResource(
                        R.string.atlas_completion_format,
                        uiState.unlockedAtlasNodes.size,
                        uiState.atlasNodes.size
                    ),
                    accent = MaterialTheme.colorScheme.primary,
                    compact = true
                )
                uiState.latestUnlockedAtlasNodeId?.let { latestId ->
                    uiState.atlasNodes.firstOrNull { it.id == latestId }?.let { latestNode ->
                        AuraFactChip(
                            text = stringResource(R.string.atlas_latest_unlock_chip, latestNode.title),
                            accent = MaterialTheme.colorScheme.secondary,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AtlasBottomPanel(
    uiState: QuizUiState,
    selectedNode: AtlasNode?,
    compact: Boolean,
    unlockedNodeIds: Set<String>,
    onSelectAtlasNode: (String) -> Unit,
    onAtlasPanelModeChanged: (AtlasPanelMode) -> Unit,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .padding(bottom = 4.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            .padding(vertical = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.atlasPanelMode == AtlasPanelMode.NODE_DETAILS,
                    onClick = { onAtlasPanelModeChanged(AtlasPanelMode.NODE_DETAILS) },
                    label = { Text(text = stringResource(R.string.atlas_panel_details)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.testTag(AuraNodeTestTags.ATLAS_PANEL_DETAILS)
                )
                FilterChip(
                    selected = uiState.atlasPanelMode == AtlasPanelMode.EXPEDITION_PROGRESS,
                    onClick = { onAtlasPanelModeChanged(AtlasPanelMode.EXPEDITION_PROGRESS) },
                    label = { Text(text = stringResource(R.string.atlas_panel_progress)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AutoGraph,
                            contentDescription = null
                        )
                    }
                )
            }

            AnimatedContent(
                targetState = uiState.atlasPanelMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "atlasPanel"
            ) { panelMode ->
                when (panelMode) {
                    AtlasPanelMode.NODE_DETAILS -> AtlasNodeDetailsPanel(
                        node = selectedNode,
                        unlockedNodeIds = unlockedNodeIds,
                        compact = compact,
                        atlasNodes = uiState.atlasNodes,
                        onSelectAtlasNode = onSelectAtlasNode
                    )

                    AtlasPanelMode.EXPEDITION_PROGRESS -> AtlasProgressPanel(
                        uiState = uiState,
                        compact = compact,
                        onSelectAtlasNode = onSelectAtlasNode
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtlasNodeDetailsPanel(
    node: AtlasNode?,
    unlockedNodeIds: Set<String>,
    compact: Boolean,
    atlasNodes: List<AtlasNode>,
    onSelectAtlasNode: (String) -> Unit
) {
    if (node == null) {
        Text(
            text = stringResource(R.string.atlas_empty_selection),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val unlocked = unlockedNodeIds.contains(node.id)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraFactChip(
                text = if (unlocked) {
                    stringResource(R.string.atlas_unlocked_status)
                } else {
                    stringResource(R.string.atlas_locked_status)
                },
                accent = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                compact = true
            )
            AuraFactChip(
                text = stringResource(node.factCategory.titleRes()),
                accent = categoryAccent(node.factCategory, MaterialTheme.colorScheme),
                compact = true
            )
            if (unlocked) {
                AuraFactChip(
                    text = node.rewardTitle,
                    accent = MaterialTheme.colorScheme.secondary,
                    compact = true
                )
            }
        }

        Text(
            text = node.title,
            style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = node.subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (unlocked) {
                node.description
            } else {
                stringResource(R.string.atlas_locked_hint, node.title)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (unlocked) {
                node.highlightFact
            } else {
                stringResource(R.string.atlas_unlock_tip)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (node.connections.isNotEmpty()) {
            Text(
                text = stringResource(R.string.atlas_connections_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                node.connections.mapNotNull { connectionId ->
                    atlasNodes.firstOrNull { it.id == connectionId }
                }.forEach { connection ->
                    AssistChip(
                        onClick = { onSelectAtlasNode(connection.id) },
                        label = { Text(text = connection.title) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtlasProgressPanel(
    uiState: QuizUiState,
    compact: Boolean,
    onSelectAtlasNode: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultMetricCard(
                label = stringResource(R.string.atlas_progress_completion),
                value = stringResource(
                    R.string.atlas_progress_percent,
                    (uiState.atlasCompletionRatio * 100).roundToInt()
                ),
                modifier = Modifier.weight(1f),
                compact = compact
            )
            ResultMetricCard(
                label = stringResource(R.string.atlas_progress_nodes),
                value = stringResource(
                    R.string.atlas_completion_format,
                    uiState.unlockedAtlasNodes.size,
                    uiState.atlasNodes.size
                ),
                modifier = Modifier.weight(1f),
                compact = compact
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultMetricCard(
                label = stringResource(R.string.atlas_progress_achievements),
                value = uiState.earnedAchievements.size.toString(),
                modifier = Modifier.weight(1f),
                compact = compact
            )
            ResultMetricCard(
                label = stringResource(R.string.atlas_progress_best_runs),
                value = uiState.hallOfFameRuns.size.toString(),
                modifier = Modifier.weight(1f),
                compact = compact
            )
        }

        uiState.latestUnlockedAtlasNodeId?.let { latestId ->
            uiState.atlasNodes.firstOrNull { it.id == latestId }?.let { latestNode ->
                AuraNodeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.atlas_latest_unlock_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = latestNode.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = latestNode.highlightFact,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AssistChip(
                            onClick = { onSelectAtlasNode(latestNode.id) },
                            label = { Text(text = stringResource(R.string.atlas_open_latest_unlock)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Explore,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }

        if (uiState.earnedAchievements.isNotEmpty()) {
            Text(
                text = stringResource(R.string.atlas_achievements_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.earnedAchievements.take(4).forEach { achievement ->
                    AchievementChip(achievement = achievement)
                }
            }
        }

        if (uiState.hallOfFameRuns.isNotEmpty()) {
            Text(
                text = stringResource(R.string.atlas_hall_of_fame_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.hallOfFameRuns.take(3).forEachIndexed { index, item ->
                    HallOfFameRunCard(
                        rank = index + 1,
                        runSummary = item
                    )
                }
            }
        }
    }
}

private fun FactCategory.titleRes(): Int = when (this) {
    FactCategory.HISTORY -> R.string.atlas_category_history
    FactCategory.CULTURE -> R.string.atlas_category_culture
    FactCategory.SCIENCE -> R.string.atlas_category_science
    FactCategory.NATURE -> R.string.atlas_category_nature
    FactCategory.TRAVEL -> R.string.atlas_category_travel
    FactCategory.INDUSTRY -> R.string.atlas_category_industry
}

@Composable
private fun categoryAccent(
    category: FactCategory,
    colors: androidx.compose.material3.ColorScheme
): Color = when (category) {
    FactCategory.HISTORY -> colors.secondary
    FactCategory.CULTURE -> colors.tertiary
    FactCategory.SCIENCE -> colors.primary
    FactCategory.NATURE -> colors.primaryContainer
    FactCategory.TRAVEL -> colors.secondaryContainer
    FactCategory.INDUSTRY -> colors.tertiaryContainer
}
