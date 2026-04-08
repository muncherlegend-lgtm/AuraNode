package com.example.newapp.ui.screens.atlas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AchievementChip
import com.example.newapp.ui.components.AtlasMapCard
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.HallOfFameRunCard
import com.example.newapp.ui.quiz.QuizUiState

@Composable
fun AtlasScreen(
    uiState: QuizUiState,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNodeId by remember(uiState.unlockedAtlasNodes, uiState.atlasNodes) {
        mutableStateOf(uiState.unlockedAtlasNodes.firstOrNull()?.id ?: uiState.atlasNodes.firstOrNull()?.id.orEmpty())
    }
    val selectedNode = uiState.atlasNodes.firstOrNull { it.id == selectedNodeId }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.ATLAS_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AuraNodeSurfaceCard(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AuraFactChip(
                            text = stringResource(R.string.atlas_chip_title),
                            compact = true
                        )
                        Text(
                            text = stringResource(R.string.atlas_title),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.atlas_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onBackToMenu) {
                            Text(text = stringResource(R.string.atlas_back_to_menu))
                        }
                    }
                }
            }

            item {
                AtlasMapCard(
                    nodes = uiState.atlasNodes,
                    unlockedNodeIds = uiState.unlockedAtlasNodes.map { it.id }.toSet(),
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = { selectedNodeId = it }
                )
            }

            selectedNode?.let { node ->
                item {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AuraFactChip(text = node.rewardTitle, compact = true)
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = node.subtitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = node.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = node.highlightFact,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (uiState.earnedAchievements.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.atlas_achievements_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.earnedAchievements.take(6).forEach { achievement ->
                            AchievementChip(achievement = achievement)
                        }
                    }
                }
            }

            if (uiState.hallOfFameRuns.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.atlas_hall_of_fame_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                itemsIndexed(uiState.hallOfFameRuns, key = { _, item -> item.timestamp }) { index, item ->
                    HallOfFameRunCard(
                        rank = index + 1,
                        runSummary = item
                    )
                }
            }
        }
    }
}
