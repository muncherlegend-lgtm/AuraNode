package com.example.newapp.ui.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.ThemePreset

@Composable
fun ThemePresetCard(
    preset: ThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuraNodeSurfaceCard(
        modifier = modifier
            .semantics { this.selected = selected }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            }
        ),
        borderColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                preset.palette.primaryContainer.toComposeColor(),
                                preset.palette.background.toComposeColor(),
                                preset.palette.secondaryContainer.toComposeColor()
                            )
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeSwatch(color = preset.palette.primary.toComposeColor())
                ThemeSwatch(color = preset.palette.secondary.toComposeColor())
                ThemeSwatch(color = preset.palette.tertiary.toComposeColor())
                if (selected) {
                    AuraFactChip(
                        text = stringResource(R.string.theme_preset_active),
                        accent = MaterialTheme.colorScheme.primary,
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .background(color = color, shape = CircleShape)
    )
}

private fun String.toComposeColor(): Color = Color(parseColor(this))
