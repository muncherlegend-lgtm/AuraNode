package com.example.newapp.ui.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object ResultShareManager {

    fun buildShareText(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): String = buildString {
        appendLine("AuraNode")
        appendLine("Результат: ${runSummary.score} / ${runSummary.maxScore}")
        appendLine("Точность: ${(runSummary.accuracyRatio * 100).roundToInt()}%")
        appendLine("Верных ответов: ${runSummary.correctAnswers} из ${runSummary.totalQuestions}")
        appendLine("Режим: ${runSummary.mode.label()}")
        appendLine("Уровень: ${runSummary.difficulty.label()}")
        themePreset?.let { appendLine("Тема: ${it.title}") }
        if (runSummary.sourceFileName.isNotBlank()) {
            appendLine("Материал: ${runSummary.sourceFileName}")
        }
        append(highlightFact)
    }

    fun shareResult(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ) {
        val shareUri = renderCardToCacheUri(
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
                buildShareText(
                    context = context,
                    runSummary = runSummary,
                    themePreset = themePreset,
                    highlightFact = highlightFact
                )
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться результатом"))
    }

    fun saveResultCard(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Uri {
        val bitmap = renderCardBitmap(runSummary, themePreset, highlightFact)
        val fileName = "auranode-result-${runSummary.timestamp}.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, bitmap, fileName)
        } else {
            saveToExternalFiles(context, bitmap, fileName)
        }
    }

    private fun renderCardToCacheUri(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Uri {
        val bitmap = renderCardBitmap(runSummary, themePreset, highlightFact)
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

    private fun saveToMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AuraNode")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = requireNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        )
        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    private fun saveToExternalFiles(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val picturesDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "AuraNode"
        ).apply { mkdirs() }
        val outputFile = File(picturesDir, fileName)
        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return Uri.fromFile(outputFile)
    }

    private fun renderCardBitmap(
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Bitmap {
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
            textSize = 68f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF314547.toInt()
            textSize = 42f
        }
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0B5561.toInt()
            textSize = 116f
            isFakeBoldText = true
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD8A449.toInt()
            textSize = 36f
            isFakeBoldText = true
        }

        canvas.drawText("Результат AuraNode", 110f, 190f, titlePaint)
        canvas.drawText(runSummary.mode.label(), 110f, 270f, badgePaint)
        canvas.drawText(runSummary.difficulty.label(), 430f, 270f, badgePaint)
        canvas.drawText("${runSummary.score} / ${runSummary.maxScore}", 110f, 430f, scorePaint)
        canvas.drawText(
            "Точность ${(runSummary.accuracyRatio * 100).roundToInt()}% • серия ${runSummary.longestStreak}",
            110f,
            520f,
            bodyPaint
        )
        if (runSummary.sourceFileName.isNotBlank()) {
            canvas.drawText("Материал: ${runSummary.sourceFileName.take(24)}", 110f, 600f, bodyPaint)
        } else {
            themePreset?.title?.let {
                canvas.drawText("Тема: $it", 110f, 600f, bodyPaint)
            }
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
        return bitmap
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

    private fun RunSummary.modeLabel(): String = mode.label()

    private fun RunSummary.difficultyLabel(): String = difficulty.label()

    private fun com.example.newapp.data.model.QuizMode.label(): String = when (this) {
        com.example.newapp.data.model.QuizMode.CLASSIC -> "Основной"
        com.example.newapp.data.model.QuizMode.SPRINT -> "Быстрый"
        com.example.newapp.data.model.QuizMode.LEGEND -> "Углублённый"
    }

    private fun com.example.newapp.data.model.Difficulty.label(): String = when (this) {
        com.example.newapp.data.model.Difficulty.CADET -> "Кадет"
        com.example.newapp.data.model.Difficulty.ENGINEER -> "Инженер"
        com.example.newapp.data.model.Difficulty.COSMONAUT -> "Космонавт"
    }

    private const val CARD_WIDTH = 1200
    private const val CARD_HEIGHT = 1600
}
