package com.example.newapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.Difficulty

@Composable
fun DifficultyRouteCard(
    difficulty: Difficulty,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val presentation = difficulty.toPresentation()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.988f else 1f,
        label = "difficultyScale"
    )

    AuraNodeSurfaceCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                presentation.container.copy(alpha = 0.98f)
            } else {
                presentation.container.copy(alpha = 0.82f)
            }
        ),
        borderColor = if (selected) {
            presentation.accent
        } else {
            presentation.accent.copy(alpha = 0.24f)
        }
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 16.dp else 22.dp,
                vertical = if (compact) 16.dp else 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraFactChip(
                    text = stringResource(presentation.badgeRes),
                    accent = presentation.accent,
                    compact = true
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.accent,
                    modifier = Modifier.size(if (compact) 22.dp else 28.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)) {
                Text(
                    text = stringResource(presentation.titleRes),
                    style = if (compact) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.Bold,
                    color = presentation.onContainer
                )
                Text(
                    text = stringResource(presentation.subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = presentation.onContainer.copy(alpha = 0.80f)
                )
            }

            if (selected) {
                AuraFactChip(
                    text = "Выбрано",
                    accent = presentation.accent,
                    compact = true
                )
            }
        }
    }
}
