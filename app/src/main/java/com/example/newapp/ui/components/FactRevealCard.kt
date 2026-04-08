package com.example.newapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.ui.quiz.AnswerFeedbackType
import com.example.newapp.ui.theme.ErrorRose
import com.example.newapp.ui.theme.SuccessFern

@Composable
fun FactRevealCard(
    visible: Boolean,
    title: String,
    body: String,
    type: AnswerFeedbackType,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accent = when (type) {
        AnswerFeedbackType.CORRECT -> SuccessFern
        AnswerFeedbackType.INCORRECT -> ErrorRose
        AnswerFeedbackType.TIMEOUT -> MaterialTheme.colorScheme.secondary
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
        modifier = modifier
    ) {
        AuraNodeSurfaceCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            ),
            borderColor = accent.copy(alpha = 0.24f)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = if (compact) 16.dp else 20.dp,
                    vertical = if (compact) 14.dp else 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
            ) {
                Text(
                    text = title,
                    style = if (compact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Text(
                    text = body,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
