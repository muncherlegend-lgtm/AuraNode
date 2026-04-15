package com.example.newapp.ui.share

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset
import java.io.File
import java.io.FileOutputStream

object ResultShareManager {

    fun buildShareText(
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): String = buildResultShareContent(
        runSummary = runSummary,
        themePreset = themePreset,
        highlightFact = highlightFact
    ).toShareText()

    fun shareResult(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Result<Unit> = runCatching {
        val content = buildResultShareContent(
            runSummary = runSummary,
            themePreset = themePreset,
            highlightFact = highlightFact
        )
        val shareUri = renderCardToCacheUri(
            context = context,
            runSummary = runSummary,
            themePreset = themePreset,
            highlightFact = highlightFact
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_TEXT, content.toShareText())
            putExtra(Intent.EXTRA_SUBJECT, "${content.packCategory}: ${content.scoreLabel}")
            clipData = ClipData.newUri(context.contentResolver, "Результат AuraNode", shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться результатом"))
    }

    fun saveResultCard(
        context: Context,
        runSummary: RunSummary,
        themePreset: ThemePreset?,
        highlightFact: String
    ): Result<Uri> = runCatching {
        val bitmap = renderCardBitmap(runSummary, themePreset, highlightFact)
        val fileName = "auranode-result-${runSummary.timestamp}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        val outputFile = File(context.cacheDir, "shared/result-card-${runSummary.timestamp}.png")
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
        val content = buildResultShareContent(
            runSummary = runSummary,
            themePreset = themePreset,
            highlightFact = highlightFact
        )
        val palette = themePreset.toBitmapPalette()
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                CARD_WIDTH.toFloat(),
                CARD_HEIGHT.toFloat(),
                intArrayOf(palette.backgroundStart, palette.backgroundMiddle, palette.backgroundEnd),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat()),
            60f,
            60f,
            backgroundPaint
        )

        drawAmbientGlow(
            canvas = canvas,
            centerX = CARD_WIDTH * 0.86f,
            centerY = CARD_HEIGHT * 0.15f,
            radius = CARD_WIDTH * 0.34f,
            color = palette.glow
        )
        drawAmbientGlow(
            canvas = canvas,
            centerX = CARD_WIDTH * 0.14f,
            centerY = CARD_HEIGHT * 0.84f,
            radius = CARD_WIDTH * 0.30f,
            color = palette.glowSecondary
        )

        val contentRect = RectF(64f, 88f, CARD_WIDTH - 64f, CARD_HEIGHT - 88f)
        canvas.drawRoundRect(
            RectF(
                contentRect.left,
                contentRect.top + 16f,
                contentRect.right,
                contentRect.bottom + 16f
            ),
            46f,
            46f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x21000000 }
        )
        canvas.drawRoundRect(
            contentRect,
            46f,
            46f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surface }
        )
        canvas.drawRoundRect(
            contentRect,
            46f,
            46f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = palette.outline
            }
        )

        val serifDisplay = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textPrimary
            textSize = 78f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textPrimary
            textSize = 48f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textSecondary
            textSize = 38f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accent
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val scorePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textPrimary
            textSize = 118f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val statValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textPrimary
            textSize = 42f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        drawPill(
            canvas = canvas,
            rect = RectF(116f, 138f, 314f, 196f),
            backgroundColor = palette.accentSoft,
            textColor = palette.accent,
            label = "AURANODE"
        )
        drawPill(
            canvas = canvas,
            rect = RectF(774f, 138f, 1048f, 196f),
            backgroundColor = palette.secondarySoft,
            textColor = palette.secondary,
            label = content.verdictLabel
        )

        var currentY = 234f
        currentY = drawTextBlock(
            canvas = canvas,
            text = content.packCategory,
            paint = sectionPaint,
            x = 116f,
            y = currentY,
            width = 620,
            maxLines = 1
        ) + 14f
        currentY = drawTextBlock(
            canvas = canvas,
            text = content.packTitle,
            paint = serifDisplay,
            x = 116f,
            y = currentY,
            width = 790,
            maxLines = 2
        ) + 26f

        val modeChipWidth = measurePillWidth(labelPaint, content.modeLabel)
        drawPill(
            canvas = canvas,
            rect = RectF(116f, currentY, 116f + modeChipWidth, currentY + 58f),
            backgroundColor = palette.accentSoft,
            textColor = palette.accent,
            label = content.modeLabel
        )
        drawPill(
            canvas = canvas,
            rect = RectF(
                132f + modeChipWidth,
                currentY,
                132f + modeChipWidth + measurePillWidth(labelPaint, content.difficultyLabel),
                currentY + 58f
            ),
            backgroundColor = palette.secondarySoft,
            textColor = palette.secondary,
            label = content.difficultyLabel
        )

        val scoreRect = RectF(116f, currentY + 84f, 1084f, currentY + 364f)
        canvas.drawRoundRect(
            scoreRect,
            34f,
            34f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    scoreRect.left,
                    scoreRect.top,
                    scoreRect.right,
                    scoreRect.bottom,
                    intArrayOf(palette.surfaceAlt, palette.surface),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        )
        drawTextBlock(
            canvas = canvas,
            text = "Итоговый счёт",
            paint = labelPaint,
            x = scoreRect.left + 40f,
            y = scoreRect.top + 34f,
            width = 400,
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = content.scoreLabel,
            paint = scorePaint,
            x = scoreRect.left + 40f,
            y = scoreRect.top + 86f,
            width = 640,
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = content.accuracyLabel,
            paint = bodyPaint,
            x = scoreRect.left + 40f,
            y = scoreRect.top + 214f,
            width = 420,
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = "${content.modeLabel} • ${content.difficultyLabel}",
            paint = bodyPaint,
            x = scoreRect.left + 40f,
            y = scoreRect.top + 260f,
            width = 520,
            maxLines = 1
        )

        val statsTop = scoreRect.bottom + 30f
        drawMetricPanel(
            canvas = canvas,
            rect = RectF(116f, statsTop, 586f, statsTop + 162f),
            label = "Верно",
            value = content.correctAnswersLabel,
            palette = palette,
            labelPaint = labelPaint,
            valuePaint = statValuePaint
        )
        drawMetricPanel(
            canvas = canvas,
            rect = RectF(614f, statsTop, 1084f, statsTop + 162f),
            label = "Темп",
            value = content.streakLabel,
            palette = palette,
            labelPaint = labelPaint,
            valuePaint = statValuePaint
        )

        val highlightRect = RectF(116f, statsTop + 196f, 1084f, statsTop + 548f)
        canvas.drawRoundRect(
            highlightRect,
            34f,
            34f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.accentSoft }
        )
        drawTextBlock(
            canvas = canvas,
            text = "Что запомнилось",
            paint = labelPaint,
            x = highlightRect.left + 34f,
            y = highlightRect.top + 30f,
            width = 380,
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = content.highlightFact,
            paint = bodyPaint.apply { color = palette.textPrimary },
            x = highlightRect.left + 34f,
            y = highlightRect.top + 88f,
            width = 900,
            maxLines = 5
        )
        bodyPaint.color = palette.textSecondary

        drawTextBlock(
            canvas = canvas,
            text = content.footerLabel,
            paint = bodyPaint,
            x = 116f,
            y = highlightRect.bottom + 44f,
            width = 900,
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = "Спокойная учебная викторина о регионе",
            paint = bodyPaint.apply { textSize = 32f },
            x = 116f,
            y = highlightRect.bottom + 94f,
            width = 900,
            maxLines = 1
        )

        return bitmap
    }

    private fun drawMetricPanel(
        canvas: Canvas,
        rect: RectF,
        label: String,
        value: String,
        palette: ShareBitmapPalette,
        labelPaint: TextPaint,
        valuePaint: TextPaint
    ) {
        canvas.drawRoundRect(
            rect,
            30f,
            30f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.surfaceAlt }
        )
        drawTextBlock(
            canvas = canvas,
            text = label,
            paint = labelPaint,
            x = rect.left + 28f,
            y = rect.top + 26f,
            width = (rect.width() - 56f).toInt(),
            maxLines = 1
        )
        drawTextBlock(
            canvas = canvas,
            text = value,
            paint = valuePaint,
            x = rect.left + 28f,
            y = rect.top + 74f,
            width = (rect.width() - 56f).toInt(),
            maxLines = 2
        )
    }

    private fun drawAmbientGlow(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        color: Int
    ) {
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                centerX,
                centerY,
                radius,
                intArrayOf(color, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, radius, glowPaint)
    }

    private fun drawPill(
        canvas: Canvas,
        rect: RectF,
        backgroundColor: Int,
        textColor: Int,
        label: String
    ) {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor }
        canvas.drawRoundRect(rect, 100f, 100f, backgroundPaint)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 26f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val layout = StaticLayout.Builder.obtain(label, 0, label.length, textPaint, rect.width().toInt() - 40)
            .setIncludePad(false)
            .setMaxLines(1)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        canvas.save()
        canvas.translate(rect.left + 20f, rect.top + (rect.height() - layout.height) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun measurePillWidth(paint: TextPaint, label: String): Float =
        paint.measureText(label) + 54f

    private fun drawTextBlock(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int,
        maxLines: Int
    ): Float {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(maxLines)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    private fun ThemePreset?.toBitmapPalette(): ShareBitmapPalette {
        val palette = this?.palette
        return ShareBitmapPalette(
            backgroundStart = palette?.primaryContainer.parseColorOr(0xFF0F535D.toInt()),
            backgroundMiddle = palette?.background.parseColorOr(0xFFF4EEE4.toInt()),
            backgroundEnd = palette?.secondaryContainer.parseColorOr(0xFFE0D0A4.toInt()),
            glow = palette?.secondary.parseColorOr(0x40D6A34A),
            glowSecondary = palette?.primary.parseColorOr(0x331B727B),
            surface = palette?.surface.parseColorOr(0xF7FFF9F1.toInt()),
            surfaceAlt = palette?.surfaceVariant.parseColorOr(0xFFDDE8E6.toInt()),
            textPrimary = palette?.onSurface.parseColorOr(0xFF172325.toInt()),
            textSecondary = palette?.onSurfaceVariant.parseColorOr(0xFF476063.toInt()),
            accent = palette?.primary.parseColorOr(0xFF0B5561.toInt()),
            accentSoft = palette?.primaryContainer.parseColorOr(0x33B9E1DE),
            secondary = palette?.secondary.parseColorOr(0xFFD6A34A.toInt()),
            secondarySoft = palette?.secondaryContainer.parseColorOr(0x33FFE7B4),
            outline = palette?.outline.parseColorOr(0x33657073)
        )
    }

    private fun String?.parseColorOr(fallback: Int): Int = runCatching {
        if (this.isNullOrBlank()) fallback else Color.parseColor(this)
    }.getOrDefault(fallback)

    private data class ShareBitmapPalette(
        val backgroundStart: Int,
        val backgroundMiddle: Int,
        val backgroundEnd: Int,
        val glow: Int,
        val glowSecondary: Int,
        val surface: Int,
        val surfaceAlt: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int,
        val accentSoft: Int,
        val secondary: Int,
        val secondarySoft: Int,
        val outline: Int
    )

    private const val CARD_WIDTH = 1200
    private const val CARD_HEIGHT = 1600
}
