package com.example.newapp.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.newapp.R
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object ResultShareManager {

    fun shareResult(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ) {
        val shareUri = renderCardToFile(
            context = context,
            runSummary = runSummary,
            themePreset = themePreset,
            highlightFact = highlightFact
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(
                Intent.EXTRA_TEXT,
                buildString {
                    appendLine(context.getString(R.string.share_card_title))
                    appendLine("${runSummary.score} / ${runSummary.maxScore}")
                    appendLine(
                        context.getString(
                            R.string.share_card_accuracy_format,
                            (runSummary.accuracyRatio * 100).roundToInt(),
                            runSummary.medalTier.name
                        )
                    )
                    append(highlightFact)
                }
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_card_chooser_title))
        )
    }

    private fun renderCardToFile(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Uri {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = if (themePreset != null) {
            intArrayOf(
                android.graphics.Color.parseColor(themePreset.palette.primaryContainer),
                android.graphics.Color.parseColor(themePreset.palette.background),
                android.graphics.Color.parseColor(themePreset.palette.secondaryContainer)
            )
        } else {
            intArrayOf(0xFF0B5561.toInt(), 0xFFF7F2E8.toInt(), 0xFFD8A449.toInt())
        }

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                CARD_WIDTH.toFloat(),
                CARD_HEIGHT.toFloat(),
                colors,
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat()),
            56f,
            56f,
            backgroundPaint
        )

        val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xF8FFFFFF.toInt()
        }
        canvas.drawRoundRect(
            RectF(64f, 88f, CARD_WIDTH - 64f, CARD_HEIGHT - 88f),
            42f,
            42f,
            surfacePaint
        )

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF172325.toInt()
            textSize = 72f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF314547.toInt()
            textSize = 42f
        }
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0B5561.toInt()
            textSize = 120f
            isFakeBoldText = true
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD8A449.toInt()
            textSize = 36f
            isFakeBoldText = true
        }

        canvas.drawText(context.getString(R.string.share_card_title), 110f, 190f, titlePaint)
        canvas.drawText(runSummary.mode.name, 110f, 270f, badgePaint)
        canvas.drawText(runSummary.medalTier.name, 420f, 270f, badgePaint)
        canvas.drawText("${runSummary.score} / ${runSummary.maxScore}", 110f, 430f, scorePaint)
        canvas.drawText(
            context.getString(
                R.string.share_card_streak_format,
                (runSummary.accuracyRatio * 100).roundToInt(),
                runSummary.longestStreak
            ),
            110f,
            520f,
            bodyPaint
        )
        themePreset?.title?.let {
            canvas.drawText(
                context.getString(R.string.share_card_theme_format, it),
                110f,
                600f,
                bodyPaint
            )
        }
        drawMultilineText(
            canvas = canvas,
            text = highlightFact,
            paint = bodyPaint,
            startX = 110f,
            startY = 700f,
            maxWidth = CARD_WIDTH - 220f,
            lineHeight = 56f
        )

        val outputFile = File(context.cacheDir, "shared/result-card.png")
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float
    ) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        words.forEach { word ->
            val candidate = if (currentLine.isBlank()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                lines += currentLine
                currentLine = word
            }
        }
        if (currentLine.isNotBlank()) lines += currentLine

        lines.take(8).forEachIndexed { index, line ->
            canvas.drawText(line, startX, startY + index * lineHeight, paint)
        }
    }

    private const val CARD_WIDTH = 1200
    private const val CARD_HEIGHT = 1600
}
