package com.example.newapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.newapp.data.model.BackgroundArtworkStyle

@Composable
fun AuraNodeBackdrop(
    modifier: Modifier = Modifier,
    backgroundStyle: BackgroundArtworkStyle = BackgroundArtworkStyle.WAVES,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryContainer.copy(alpha = 0.48f),
                        colors.background,
                        colors.secondaryContainer.copy(alpha = 0.26f)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAmbientGlow(colors)
            when (backgroundStyle) {
                BackgroundArtworkStyle.WAVES -> drawWaveBands(colors)
                BackgroundArtworkStyle.MOUNTAINS -> drawMountainLayers(colors)
                BackgroundArtworkStyle.CONSTELLATION -> drawConstellationField(colors)
            }
        }

        content()
    }
}

private fun DrawScope.drawAmbientGlow(colors: ColorScheme) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.secondary.copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.82f, size.height * 0.16f),
            radius = size.minDimension * 0.34f
        ),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.82f, size.height * 0.16f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.primary.copy(alpha = 0.14f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.12f, size.height * 0.78f),
            radius = size.minDimension * 0.42f
        ),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.12f, size.height * 0.78f)
    )
}

private fun DrawScope.drawWaveBands(colors: ColorScheme) {
    val wavePath = Path().apply {
        moveTo(0f, size.height * 0.25f)
        quadraticTo(
            size.width * 0.18f,
            size.height * 0.18f,
            size.width * 0.36f,
            size.height * 0.26f
        )
        quadraticTo(
            size.width * 0.56f,
            size.height * 0.34f,
            size.width * 0.72f,
            size.height * 0.24f
        )
        quadraticTo(
            size.width * 0.88f,
            size.height * 0.18f,
            size.width,
            size.height * 0.28f
        )
    }

    val lowerWavePath = Path().apply {
        moveTo(0f, size.height * 0.74f)
        quadraticTo(
            size.width * 0.22f,
            size.height * 0.66f,
            size.width * 0.44f,
            size.height * 0.76f
        )
        quadraticTo(
            size.width * 0.68f,
            size.height * 0.86f,
            size.width,
            size.height * 0.72f
        )
    }

    drawPath(
        path = wavePath,
        brush = Brush.linearGradient(
            colors = listOf(
                colors.primary.copy(alpha = 0.18f),
                colors.secondary.copy(alpha = 0.12f)
            )
        ),
        style = Stroke(width = size.minDimension * 0.04f, cap = StrokeCap.Round)
    )

    drawPath(
        path = lowerWavePath,
        brush = Brush.linearGradient(
            colors = listOf(
                colors.tertiary.copy(alpha = 0.16f),
                colors.primary.copy(alpha = 0.10f)
            )
        ),
        style = Stroke(width = size.minDimension * 0.055f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMountainLayers(colors: ColorScheme) {
    val backRange = Path().apply {
        moveTo(0f, size.height * 0.58f)
        lineTo(size.width * 0.16f, size.height * 0.44f)
        lineTo(size.width * 0.34f, size.height * 0.56f)
        lineTo(size.width * 0.54f, size.height * 0.38f)
        lineTo(size.width * 0.72f, size.height * 0.54f)
        lineTo(size.width, size.height * 0.42f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    val frontRange = Path().apply {
        moveTo(0f, size.height * 0.74f)
        lineTo(size.width * 0.18f, size.height * 0.60f)
        lineTo(size.width * 0.33f, size.height * 0.70f)
        lineTo(size.width * 0.48f, size.height * 0.55f)
        lineTo(size.width * 0.66f, size.height * 0.72f)
        lineTo(size.width * 0.84f, size.height * 0.58f)
        lineTo(size.width, size.height * 0.68f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    drawPath(
        path = backRange,
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.tertiary.copy(alpha = 0.20f),
                colors.primary.copy(alpha = 0.09f)
            )
        )
    )

    drawPath(
        path = frontRange,
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.secondary.copy(alpha = 0.14f),
                colors.primary.copy(alpha = 0.08f)
            )
        )
    )
}

private fun DrawScope.drawConstellationField(colors: ColorScheme) {
    val stars = listOf(
        Offset(size.width * 0.18f, size.height * 0.18f),
        Offset(size.width * 0.32f, size.height * 0.11f),
        Offset(size.width * 0.46f, size.height * 0.20f),
        Offset(size.width * 0.66f, size.height * 0.15f),
        Offset(size.width * 0.80f, size.height * 0.24f),
        Offset(size.width * 0.72f, size.height * 0.36f),
        Offset(size.width * 0.22f, size.height * 0.34f)
    )

    stars.zipWithNext().forEach { (start, end) ->
        drawLine(
            color = colors.secondary.copy(alpha = 0.14f),
            start = start,
            end = end,
            strokeWidth = size.minDimension * 0.0032f,
            cap = StrokeCap.Round
        )
    }

    stars.forEachIndexed { index, offset ->
        val radiusMultiplier = if (index % 2 == 0) 0.006f else 0.0044f
        drawCircle(
            color = colors.onBackground.copy(alpha = 0.88f),
            radius = size.minDimension * radiusMultiplier,
            center = offset
        )
    }

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                colors.primary.copy(alpha = 0.06f)
            ),
            start = Offset(0f, size.height * 0.08f),
            end = Offset(size.width, size.height * 0.78f)
        )
    )
}
