package com.example.newapp.data.source

import android.text.Html
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream

internal object DocumentTextExtraction {

    fun extractPlainText(inputStream: InputStream): String =
        sanitizeImportedText(inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })

    fun extractDocxText(bytes: ByteArray): String {
        val documentXml = ZipInputStream(ByteArrayInputStream(bytes)).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }
                .firstOrNull { it.name == DOCX_DOCUMENT_XML }
                ?.let { zipInputStream.bufferedReader(Charsets.UTF_8).use { reader -> reader.readText() } }
                .orEmpty()
        }
        if (documentXml.isBlank()) return ""

        val normalizedXml = documentXml
            .replace("</w:p>", "\n")
            .replace("</w:tr>", "\n")
            .replace("</w:tc>", " ")

        val content = TEXT_NODE_REGEX.findAll(normalizedXml)
            .joinToString(separator = " ") { matchResult -> matchResult.groupValues[1] }

        val decoded = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString()
        return sanitizeImportedText(decoded)
    }

    fun sanitizeImportedText(rawValue: String): String = rawValue
        .replace('\u0000', ' ')
        .replace('\r', '\n')
        .replace(TABS_AND_SPACES_REGEX, " ")
        .replace(THREE_PLUS_NEWLINES_REGEX, "\n\n")
        .lines()
        .map { it.trim() }
        .filter(String::isNotBlank)
        .joinToString(separator = "\n")
        .trim()

    fun resolveFileExtension(displayName: String, mimeType: String): String {
        val normalizedName = displayName.lowercase(Locale.ROOT)
        return when {
            normalizedName.endsWith(".txt") -> "txt"
            normalizedName.endsWith(".md") || mimeType.contains("markdown", ignoreCase = true) -> "md"
            normalizedName.endsWith(".pdf") || mimeType == "application/pdf" -> "pdf"
            normalizedName.endsWith(".docx") -> "docx"
            mimeType.startsWith("text/") -> "txt"
            else -> ""
        }
    }

    private const val DOCX_DOCUMENT_XML = "word/document.xml"
    private val TEXT_NODE_REGEX = Regex("<w:t[^>]*>(.*?)</w:t>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val TABS_AND_SPACES_REGEX = Regex("[\\t\\x0B\\f ]+")
    private val THREE_PLUS_NEWLINES_REGEX = Regex("\\n{3,}")
}
