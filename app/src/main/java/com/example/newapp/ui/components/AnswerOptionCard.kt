package com.example.newapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.ui.theme.AuraNodeCorners
import com.example.newapp.ui.theme.ErrorRose
import com.example.newapp.ui.theme.SuccessFern

@Composable
fun AnswerOptionCard(
    optionIndex: Int,
    optionText: String,
    selectedAnswerIndex: Int?,
    revealedAnswerIndex: Int?,
    isAnswerLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isCorrectAnswer = revealedAnswerIndex == optionIndex
    val isSelectedWrongAnswer = isAnswerLocked &&
        selectedAnswerIndex == optionIndex &&
        selectedAnswerIndex != revealedAnswerIndex

    val targetContainer = when {
        isCorrectAnswer -> SuccessFern.copy(alpha = 0.16f)
        isSelectedWrongAnswer -> ErrorRose.copy(alpha = 0.14f)
        isPressed && !isAnswerLocked -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    }

    val targetBorder = when {
        isCorrectAnswer -> SuccessFern
        isSelectedWrongAnswer -> ErrorRose
        selectedAnswerIndex == optionIndex && !isAnswerLocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    }

    val targetNumberColor = when {
        isCorrectAnswer -> SuccessFern
        isSelectedWrongAnswer -> ErrorRose
        else -> MaterialTheme.colorScheme.primary
    }

    val containerColor by animateColorAsState(targetValue = targetContainer, label = "answerContainer")
    val borderColor by animateColorAsState(targetValue = targetBorder, label = "answerBorder")
    val numberColor by animateColorAsState(targetValue = targetNumberColor, label = "answerNumber")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isAnswerLocked && !isCorrectAnswer && !isSelectedWrongAnswer) 0.82f else 1f)
            .clickable(
                enabled = !isAnswerLocked,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = AuraNodeCorners.card,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 14.dp else 18.dp,
                    vertical = if (compact) 14.dp else 18.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = numberColor.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = (optionIndex + 1).toString(),
                    modifier = Modifier.padding(
                        horizontal = if (compact) 10.dp else 12.dp,
                        vertical = if (compact) 6.dp else 8.dp
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = numberColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = optionText,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCorrectAnswer) {
                    Text(
                        text = stringResource(R.string.quiz_answer_state_correct),
                        style = MaterialTheme.typography.labelMedium,
                        color = SuccessFern
                    )
                } else if (isSelectedWrongAnswer) {
                    Text(
                        text = stringResource(R.string.quiz_answer_state_incorrect),
                        style = MaterialTheme.typography.labelMedium,
                        color = ErrorRose
                    )
                }
            }
        }
    }
}
