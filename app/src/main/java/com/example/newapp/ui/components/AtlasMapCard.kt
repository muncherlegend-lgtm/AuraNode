package com.example.newapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.AtlasNode
import kotlin.math.hypot

@Composable
fun AtlasMapCard(
    nodes: List<AtlasNode>,
    unlockedNodeIds: Set<String>,
    selectedNodeId: String?,
    onNodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    AuraNodeSurfaceCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(nodes, unlockedNodeIds, selectedNodeId) {
                        detectTapGestures { tapOffset ->
                            val sizeWidth = size.width.toFloat()
                            val sizeHeight = size.height.toFloat()
                            nodes.firstOrNull { node ->
                                val center = Offset(node.xFraction * sizeWidth, node.yFraction * sizeHeight)
                                hypot(
                                    (center.x - tapOffset.x).toDouble(),
                                    (center.y - tapOffset.y).toDouble()
                                ) <= 34.0
                            }?.let { onNodeSelected(it.id) }
                        }
                    }
            ) {
                val nodeMap = nodes.associateBy { it.id }
                nodes.forEach { node ->
                    val start = Offset(node.xFraction * size.width, node.yFraction * size.height)
                    node.connections.forEach { connectionId ->
                        val destination = nodeMap[connectionId] ?: return@forEach
                        val end = Offset(destination.xFraction * size.width, destination.yFraction * size.height)
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colors.primary.copy(alpha = 0.22f),
                                    colors.secondary.copy(alpha = 0.18f)
                                )
                            ),
                            start = start,
                            end = end,
                            strokeWidth = 6f
                        )
                    }
                }

                nodes.forEach { node ->
                    val center = Offset(node.xFraction * size.width, node.yFraction * size.height)
                    val unlocked = unlockedNodeIds.contains(node.id)
                    val selected = selectedNodeId == node.id
                    drawCircle(
                        color = if (unlocked) {
                            colors.secondary.copy(alpha = 0.28f)
                        } else {
                            colors.surfaceVariant.copy(alpha = 0.72f)
                        },
                        center = center,
                        radius = if (selected) 28f else 22f
                    )
                    drawCircle(
                        color = if (selected) {
                            colors.primary
                        } else if (unlocked) {
                            colors.secondary
                        } else {
                            colors.outline
                        },
                        center = center,
                        radius = if (selected) 16f else 12f,
                        style = Stroke(width = if (selected) 8f else 6f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.atlas_chip_title),
                modifier = Modifier.align(Alignment.TopStart),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
