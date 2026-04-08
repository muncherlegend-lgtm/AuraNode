package com.example.newapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.RunSummary

@Composable
fun HallOfFameRunCard(
    rank: Int,
    runSummary: RunSummary,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuraFactChip(text = "#$rank", compact = true)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (runSummary.packTitle.isNotBlank()) {
                        "${runSummary.packTitle} · ${runSummary.difficulty.name}"
                    } else {
                        "${runSummary.mode.name} · ${runSummary.difficulty.name}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${runSummary.score} / ${runSummary.maxScore}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append("Точность ${(runSummary.accuracyRatio * 100).toInt()}% · серия ${runSummary.longestStreak}")
                        if (runSummary.sourceFileName.isNotBlank()) {
                            append(" · ${runSummary.sourceFileName}")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AuraFactChip(
                text = runSummary.medalTier.name,
                accent = MaterialTheme.colorScheme.secondary,
                compact = true
            )
        }
    }
}
