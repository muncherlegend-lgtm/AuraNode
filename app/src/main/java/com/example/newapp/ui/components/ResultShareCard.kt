package com.example.newapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import com.example.newapp.ui.theme.AuraNodeCorners

@Composable
fun ResultShareCard(
    runSummary: RunSummary,
    themePreset: ThemePreset?,
    highlightFact: String,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f),
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = AuraNodeCorners.card
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraFactChip(text = runSummary.mode.name, compact = true)
                AuraFactChip(
                    text = runSummary.medalTier.name,
                    accent = MaterialTheme.colorScheme.secondary,
                    compact = true
                )
            }
            Text(
                text = "AuraNode Expedition",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${runSummary.score} / ${runSummary.maxScore}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Точность ${(runSummary.accuracyRatio * 100).toInt()}% · серия ${runSummary.longestStreak}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = highlightFact,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            themePreset?.let {
                Text(
                    text = "Тема: ${it.title}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
