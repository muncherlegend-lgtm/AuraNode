package com.example.newapp.data.source

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.newapp.data.model.ImportedDocument
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DocumentImportDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun prepareImportedDocument(uri: Uri): ImportedDocument = withContext(Dispatchers.IO) {
        val displayName = resolveDisplayName(uri)
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val extension = DocumentTextExtraction.resolveFileExtension(displayName, mimeType)
        require(extension in SUPPORTED_EXTENSIONS) {
            "Unsupported file type: $displayName"
        }

        val extractedText = when (extension) {
            "txt", "md" -> context.contentResolver.openInputStream(uri)?.use {
                DocumentTextExtraction.extractPlainText(it)
            }.orEmpty()

            "docx" -> context.contentResolver.openInputStream(uri)?.use { inputStream ->
                DocumentTextExtraction.extractDocxText(inputStream.readBytes())
            }.orEmpty()

            "pdf" -> extractPdfText(uri)
            else -> ""
        }

        val sanitizedText = DocumentTextExtraction.sanitizeImportedText(extractedText)
        require(sanitizedText.length >= MIN_TEXT_LENGTH) {
            "Not enough text to generate a quiz."
        }

        ImportedDocument(
            displayName = displayName,
            mimeType = mimeType.ifBlank { extensionToMime(extension) },
            sourceExtension = extension,
            extractedText = sanitizedText,
            previewExcerpt = sanitizedText.take(PREVIEW_LENGTH),
            estimatedQuestionCount = (sanitizedText.length / 420).coerceIn(6, 18)
        )
    }

    private fun extractPdfText(uri: Uri): String {
        PDFBoxResourceLoader.init(context)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                return DocumentTextExtraction.sanitizeImportedText(PDFTextStripper().getText(document))
            }
        }
        return ""
    }

    private fun resolveDisplayName(uri: Uri): String =
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(displayNameIndex)
            } else {
                uri.lastPathSegment?.substringAfterLast('/') ?: "imported_material"
            }
        } ?: "imported_material"

    private fun extensionToMime(extension: String): String = when (extension.lowercase(Locale.ROOT)) {
        "txt" -> "text/plain"
        "md" -> "text/markdown"
        "docx" -> DOCX_MIME
        "pdf" -> PDF_MIME
        else -> "application/octet-stream"
    }

    private companion object {
        const val MIN_TEXT_LENGTH = 140
        const val PREVIEW_LENGTH = 240
        const val DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val PDF_MIME = "application/pdf"
        val SUPPORTED_EXTENSIONS = setOf("txt", "md", "pdf", "docx")
    }
}
