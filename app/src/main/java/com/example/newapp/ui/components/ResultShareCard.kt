package com.example.newapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import com.example.newapp.ui.copy.APP_NAME
import com.example.newapp.ui.share.buildResultShareContent
import com.example.newapp.ui.theme.AuraNodeCorners

@Composable
fun ResultShareCard(
    runSummary: RunSummary,
    themePreset: ThemePreset?,
    highlightFact: String,
    modifier: Modifier = Modifier
) {
    val content = remember(runSummary, themePreset, highlightFact) {
        buildResultShareContent(
            runSummary = runSummary,
            themePreset = themePreset,
            highlightFact = highlightFact
        )
    }
    val verdictAccent = medalAccent(runSummary.medalTier)

    AuraNodeSurfaceCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        ),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.44f)
                        )
                    ),
                    shape = AuraNodeCorners.card
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .padding(56.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        shape = CircleShape
                    )
                    .padding(46.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = APP_NAME,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = content.packCategory,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = content.packTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = verdictAccent.copy(alpha = 0.14f),
                                shape = AuraNodeCorners.pill
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = content.verdictLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = verdictAccent
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraFactChip(text = content.modeLabel, compact = true)
                    AuraFactChip(
                        text = content.difficultyLabel,
                        accent = MaterialTheme.colorScheme.secondary,
                        compact = true
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            shape = AuraNodeCorners.hero
                        )
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Итоговый счёт",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = content.scoreLabel,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = content.accuracyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShareStatCard(
                        label = "Верно",
                        value = content.correctAnswersLabel,
                        modifier = Modifier.weight(1f)
                    )
                    ShareStatCard(
                        label = "Темп",
                        value = content.streakLabel,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                            shape = AuraNodeCorners.card
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Что запомнилось",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = content.highlightFact,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = content.footerLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Спокойная учебная викторина о регионе",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
                shape = AuraNodeCorners.card
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun medalAccent(medalTier: MedalTier): Color = when (medalTier) {
    MedalTier.AURORA -> MaterialTheme.colorScheme.secondary
    MedalTier.GOLD -> MaterialTheme.colorScheme.secondary
    MedalTier.SILVER -> MaterialTheme.colorScheme.tertiary
    MedalTier.BRONZE -> MaterialTheme.colorScheme.primary
    MedalTier.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
}
