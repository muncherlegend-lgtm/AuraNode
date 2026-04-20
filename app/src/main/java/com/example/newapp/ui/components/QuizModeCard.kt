package com.example.newapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.QuizMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizModeCard(
    mode: QuizMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = mode.toPresentation()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.68f)
            .semantics { this.selected = selected }
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (selected) presentation.accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.accent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = presentation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (enabled) {
                    presentation.body
                } else {
                    stringResource(R.string.mode_legend_locked_body)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (enabled) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presentation.highlights.forEach { label ->
                        AuraFactChip(
                            text = label,
                            accent = presentation.accent,
                            compact = true
                        )
                    }
                }
            }
            if (selected) {
                AuraFactChip(
                    text = stringResource(R.string.mode_active_label),
                    accent = presentation.accent,
                    compact = true
                )
            }
        }
    }
}
