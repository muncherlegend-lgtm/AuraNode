package com.example.newapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.FactCategory
import kotlin.math.hypot

@Composable
fun AtlasMapCard(
    nodes: List<AtlasNode>,
    unlockedNodeIds: Set<String>,
    selectedNodeId: String?,
    latestUnlockedNodeId: String?,
    focusRequestId: Long,
    motionEnabled: Boolean,
    onNodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val labelFontSize = MaterialTheme.typography.labelLarge.fontSize
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var scaleTarget by rememberSaveable { mutableFloatStateOf(1f) }
    var panXTarget by rememberSaveable { mutableFloatStateOf(0f) }
    var panYTarget by rememberSaveable { mutableFloatStateOf(0f) }

    val animationSpec = if (motionEnabled) tween<Float>(durationMillis = 380) else snap()
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = animationSpec,
        label = "atlasScale"
    )
    val panX by animateFloatAsState(
        targetValue = panXTarget,
        animationSpec = animationSpec,
        label = "atlasPanX"
    )
    val panY by animateFloatAsState(
        targetValue = panYTarget,
        animationSpec = animationSpec,
        label = "atlasPanY"
    )

    val pulse by rememberInfiniteTransition(label = "atlasPulse").animateFloat(
        initialValue = if (motionEnabled) 0.92f else 1f,
        targetValue = if (motionEnabled) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "atlasPulseValue"
    )

    fun resetCamera() {
        scaleTarget = 1f
        panXTarget = 0f
        panYTarget = 0f
    }

    fun focusNode(nodeId: String?) {
        if (nodeId == null || viewportSize == IntSize.Zero) return
        val node = nodes.firstOrNull { it.id == nodeId } ?: return
        val viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat())
        val desiredScale = 1.28f
        val desiredPan = clampPan(
            pan = focusPanFor(node = node, viewport = viewport, scale = desiredScale),
            scale = desiredScale,
            viewportSize = viewportSize
        )
        scaleTarget = desiredScale
        panXTarget = desiredPan.x
        panYTarget = desiredPan.y
    }

    LaunchedEffect(focusRequestId, selectedNodeId, viewportSize) {
        if (focusRequestId > 0L) {
            focusNode(selectedNodeId ?: latestUnlockedNodeId)
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scaleTarget * zoomChange).coerceIn(1f, 2.8f)
        val clampedPan = clampPan(
            pan = Offset(panXTarget + panChange.x, panYTarget + panChange.y),
            scale = newScale,
            viewportSize = viewportSize
        )
        scaleTarget = newScale
        panXTarget = clampedPan.x
        panYTarget = clampedPan.y
    }

    val currentOnNodeSelected by rememberUpdatedState(onNodeSelected)

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.surface.copy(alpha = 0.86f),
                        colors.surfaceVariant.copy(alpha = 0.82f)
                    )
                )
            )
            .onSizeChanged { viewportSize = it }
            .transformable(state = transformableState)
            .pointerInput(nodes, scale, panX, panY, viewportSize) {
                detectMapGestures(
                    viewportSize = viewportSize,
                    scale = scale,
                    pan = Offset(panX, panY),
                    nodes = nodes,
                    onTapNode = { currentOnNodeSelected(it) },
                    onDoubleTapNode = {
                        currentOnNodeSelected(it)
                        focusNode(it)
                    },
                    onReset = ::resetCamera
                )
            }
            .testTag("atlas_map")
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            clipRect {
                val viewport = size
                val viewportCenter = Offset(viewport.width / 2f, viewport.height / 2f)
                val pan = Offset(panX, panY)
                val nodeMap = nodes.associateBy { it.id }
                val selectedNode = nodes.firstOrNull { it.id == selectedNodeId }

                withTransform({
                    translate(left = pan.x, top = pan.y)
                    scale(scaleX = scale, scaleY = scale, pivot = viewportCenter)
                }) {
                    drawMapSilhouette(colors)
                    drawMeridianHints(colors)

                    val drawnConnections = mutableSetOf<Pair<String, String>>()
                    nodes.forEach { node ->
                        val start = node.toBaseOffset(viewport)
                        node.connections.forEach { connectionId ->
                            val destination = nodeMap[connectionId] ?: return@forEach
                            val edgeKey = if (node.id < destination.id) {
                                node.id to destination.id
                            } else {
                                destination.id to node.id
                            }
                            if (!drawnConnections.add(edgeKey)) return@forEach

                            val end = destination.toBaseOffset(viewport)
                            val discovered = unlockedNodeIds.contains(node.id) &&
                                unlockedNodeIds.contains(destination.id)
                            val highlighted = selectedNode?.id == node.id || selectedNode?.id == destination.id
                            val strokeWidth = when {
                                highlighted -> 8f
                                discovered -> 5.6f
                                else -> 3.6f
                            }
                            val lineBrush = when {
                                highlighted -> Brush.linearGradient(
                                    listOf(colors.primary, colors.secondary)
                                )

                                discovered -> Brush.linearGradient(
                                    listOf(colors.secondary.copy(alpha = 0.76f), colors.primary.copy(alpha = 0.54f))
                                )

                                else -> Brush.linearGradient(
                                    listOf(colors.outline.copy(alpha = 0.28f), colors.outline.copy(alpha = 0.14f))
                                )
                            }

                            drawLine(
                                brush = lineBrush,
                                start = start,
                                end = end,
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    nodes.forEach { node ->
                        val center = node.toBaseOffset(viewport)
                        val unlocked = unlockedNodeIds.contains(node.id)
                        val selected = selectedNodeId == node.id
                        val latest = latestUnlockedNodeId == node.id
                        val categoryColor = categoryColor(node.factCategory, colors)
                        val glowRadius = when {
                            selected -> 38f * pulse
                            latest -> 32f * pulse
                            unlocked -> 24f
                            else -> 18f
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    categoryColor.copy(alpha = if (selected || latest) 0.36f else 0.18f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = glowRadius
                            ),
                            radius = glowRadius,
                            center = center,
                            blendMode = BlendMode.SrcOver
                        )

                        drawCircle(
                            color = if (unlocked) {
                                categoryColor.copy(alpha = if (selected) 0.28f else 0.16f)
                            } else {
                                colors.surfaceVariant.copy(alpha = 0.74f)
                            },
                            center = center,
                            radius = if (selected) 22f else 18f
                        )

                        drawCircle(
                            color = if (selected) {
                                colors.primary
                            } else if (unlocked) {
                                categoryColor
                            } else {
                                colors.outline.copy(alpha = 0.8f)
                            },
                            center = center,
                            radius = if (selected) 13f else 10f
                        )

                        drawCircle(
                            color = if (selected || latest) {
                                colors.onPrimary
                            } else {
                                colors.surface
                            },
                            center = center,
                            radius = if (selected) 5.5f else 4.2f
                        )

                        if (latest) {
                            drawCircle(
                                color = colors.secondary.copy(alpha = 0.72f),
                                center = center,
                                radius = 26f * pulse,
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }

                nodes.forEach { node ->
                    val labelPoint = baseToScreen(
                        basePoint = Offset(
                            node.labelXFraction * viewport.width,
                            node.labelYFraction * viewport.height
                        ),
                        viewport = viewport,
                        scale = scale,
                        pan = pan
                    )
                    val labelColor = when {
                        node.id == selectedNodeId -> colors.onSurface
                        unlockedNodeIds.contains(node.id) -> colors.onSurface.copy(alpha = 0.88f)
                        else -> colors.onSurfaceVariant.copy(alpha = 0.72f)
                    }

                    drawText(
                        textMeasurer = textMeasurer,
                        text = AnnotatedString(node.title),
                        topLeft = Offset(
                            x = labelPoint.x.coerceIn(12f, viewport.width - 150f),
                            y = labelPoint.y.coerceIn(12f, viewport.height - 40f)
                        ),
                        style = TextStyle(
                            color = labelColor,
                            fontSize = labelFontSize,
                            fontWeight = if (node.id == selectedNodeId) FontWeight.Bold else FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        MapBadge(
            text = "Жесты: перемещайте карту, масштабируйте и нажимайте на точки",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )

        FilledTonalIconButton(
            onClick = ::resetCamera,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.CenterFocusStrong,
                contentDescription = "Сбросить обзор"
            )
        }
    }
}

@Composable
private fun MapBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.wrapContentSize()
        )
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectMapGestures(
    viewportSize: IntSize,
    scale: Float,
    pan: Offset,
    nodes: List<AtlasNode>,
    onTapNode: (String) -> Unit,
    onDoubleTapNode: (String) -> Unit,
    onReset: () -> Unit
) {
    detectTapGestures(
        onTap = { tapOffset ->
            findTappedNode(
                tapOffset = tapOffset,
                viewportSize = viewportSize,
                scale = scale,
                pan = pan,
                nodes = nodes
            )?.let(onTapNode)
        },
        onDoubleTap = { tapOffset ->
            val tappedNode = findTappedNode(
                tapOffset = tapOffset,
                viewportSize = viewportSize,
                scale = scale,
                pan = pan,
                nodes = nodes
            )
            if (tappedNode != null) {
                onDoubleTapNode(tappedNode)
            } else {
                onReset()
            }
        }
    )
}

private fun findTappedNode(
    tapOffset: Offset,
    viewportSize: IntSize,
    scale: Float,
    pan: Offset,
    nodes: List<AtlasNode>
): String? {
    if (viewportSize == IntSize.Zero || nodes.isEmpty()) return null
    val viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat())
    val basePoint = screenToBase(tapOffset, viewport, scale, pan)
    val threshold = 28f / scale.coerceAtLeast(1f)

    return nodes.minByOrNull { node ->
        val center = node.toBaseOffset(viewport)
        hypot((center.x - basePoint.x).toDouble(), (center.y - basePoint.y).toDouble())
    }?.takeIf { node ->
        val center = node.toBaseOffset(viewport)
        hypot((center.x - basePoint.x).toDouble(), (center.y - basePoint.y).toDouble()) <= threshold
    }?.id
}

private fun DrawScope.drawMapSilhouette(colors: androidx.compose.material3.ColorScheme) {
    val terrain = Path().apply {
        moveTo(size.width * 0.04f, size.height * 0.82f)
        quadraticTo(size.width * 0.12f, size.height * 0.66f, size.width * 0.24f, size.height * 0.72f)
        quadraticTo(size.width * 0.36f, size.height * 0.80f, size.width * 0.42f, size.height * 0.62f)
        quadraticTo(size.width * 0.52f, size.height * 0.36f, size.width * 0.66f, size.height * 0.42f)
        quadraticTo(size.width * 0.82f, size.height * 0.48f, size.width * 0.92f, size.height * 0.22f)
        lineTo(size.width * 0.96f, size.height * 0.26f)
        lineTo(size.width * 0.90f, size.height * 0.72f)
        lineTo(size.width * 0.68f, size.height * 0.94f)
        lineTo(size.width * 0.34f, size.height * 0.90f)
        close()
    }

    val river = Path().apply {
        moveTo(size.width * 0.18f, size.height * 0.18f)
        cubicTo(
            size.width * 0.28f, size.height * 0.26f,
            size.width * 0.35f, size.height * 0.45f,
            size.width * 0.42f, size.height * 0.56f
        )
        cubicTo(
            size.width * 0.48f, size.height * 0.66f,
            size.width * 0.56f, size.height * 0.74f,
            size.width * 0.62f, size.height * 0.88f
        )
    }

    drawPath(
        path = terrain,
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.primary.copy(alpha = 0.06f),
                colors.secondary.copy(alpha = 0.08f),
                colors.tertiary.copy(alpha = 0.05f)
            )
        )
    )

    drawPath(
        path = terrain,
        color = colors.outline.copy(alpha = 0.16f),
        style = Stroke(width = 5f)
    )

    drawPath(
        path = river,
        brush = Brush.linearGradient(
            colors = listOf(colors.primary.copy(alpha = 0.44f), colors.secondary.copy(alpha = 0.26f))
        ),
        style = Stroke(width = 9f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMeridianHints(colors: androidx.compose.material3.ColorScheme) {
    repeat(4) { index ->
        val y = size.height * (0.18f + (index * 0.18f))
        drawLine(
            color = colors.outline.copy(alpha = 0.1f),
            start = Offset(size.width * 0.06f, y),
            end = Offset(size.width * 0.94f, y),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 18f))
        )
    }
}

private fun categoryColor(
    category: FactCategory,
    colors: androidx.compose.material3.ColorScheme
): Color = when (category) {
    FactCategory.HISTORY -> colors.secondary
    FactCategory.CULTURE -> colors.tertiary
    FactCategory.SCIENCE -> colors.primary
    FactCategory.NATURE -> colors.primaryContainer
    FactCategory.TRAVEL -> colors.secondaryContainer
    FactCategory.INDUSTRY -> colors.tertiaryContainer
}

private fun AtlasNode.toBaseOffset(viewport: Size): Offset =
    Offset(xFraction * viewport.width, yFraction * viewport.height)

private fun baseToScreen(
    basePoint: Offset,
    viewport: Size,
    scale: Float,
    pan: Offset
): Offset {
    val center = Offset(viewport.width / 2f, viewport.height / 2f)
    return center + (basePoint - center) * scale + pan
}

private fun screenToBase(
    screenPoint: Offset,
    viewport: Size,
    scale: Float,
    pan: Offset
): Offset {
    val center = Offset(viewport.width / 2f, viewport.height / 2f)
    return center + (screenPoint - center - pan) / scale
}

private fun focusPanFor(
    node: AtlasNode,
    viewport: Size,
    scale: Float
): Offset {
    val center = Offset(viewport.width / 2f, viewport.height / 2f)
    val basePoint = node.toBaseOffset(viewport)
    return (center - basePoint) * scale
}

private fun clampPan(
    pan: Offset,
    scale: Float,
    viewportSize: IntSize
): Offset {
    if (viewportSize == IntSize.Zero) return pan
    val width = viewportSize.width.toFloat()
    val height = viewportSize.height.toFloat()
    val maxX = ((width * scale - width) / 2f + width * 0.08f).coerceAtLeast(0f)
    val maxY = ((height * scale - height) / 2f + height * 0.08f).coerceAtLeast(0f)
    return Offset(
        x = pan.x.coerceIn(-maxX, maxX),
        y = pan.y.coerceIn(-maxY, maxY)
    )
}
