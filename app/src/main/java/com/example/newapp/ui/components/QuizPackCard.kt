package com.example.newapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.QuizPackSummary
import com.example.newapp.data.model.QuizPackType

@Composable
fun QuizPackCard(
    pack: QuizPackSummary,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(
        modifier = modifier.semantics { this.selected = selected }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraFactChip(
                    text = if (pack.type == QuizPackType.OFFICIAL_ALTAI) {
                        "Основной набор"
                    } else {
                        "Пользовательский набор"
                    },
                    accent = MaterialTheme.colorScheme.primary,
                    compact = true
                )
                AuraFactChip(
                    text = when (pack.generationSource) {
                        PackGenerationSource.OFFICIAL -> "Встроенный"
                        PackGenerationSource.CLOUD_AI -> "Пользовательский"
                        PackGenerationSource.OFFLINE_DRAFT -> "Локальный"
                    },
                    accent = MaterialTheme.colorScheme.secondary,
                    compact = true
                )
            }
            Text(
                text = pack.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pack.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (pack.sourceFileName.isNotBlank()) {
                Text(
                    text = pack.sourceFileName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            if (pack.coverFact.isNotBlank()) {
                Text(
                    text = pack.coverFact,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (selected) "Выбран" else "Открыть")
                }
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Удалить")
                    }
                }
            }
        }
    }
}
