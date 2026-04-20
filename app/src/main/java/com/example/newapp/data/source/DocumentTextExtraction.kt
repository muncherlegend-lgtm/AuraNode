package com.example.newapp.data.source

import android.text.Html
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.Locale
import java.util.zip.ZipInputStream

internal object DocumentTextExtraction {

    fun extractPlainText(inputStream: InputStream): String =
        sanitizeImportedText(decodeBestEffort(inputStream.readBytes()))

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
        .replace('\u00A0', ' ')
        .replace('\u200B', ' ')
        .replace('\u00AD', ' ')
        .replace('\r', '\n')
        .replace(MARKDOWN_LINK_REGEX, "$1")
        .replace(MARKDOWN_IMAGE_REGEX, "$1")
        .replace(MARKDOWN_CODE_REGEX, "$1")
        .replace(QUOTE_PREFIX_REGEX, "")
        .replace(HEADING_PREFIX_REGEX, "")
        .replace(BULLET_PREFIX_REGEX, "• ")
        .replace(HYPHENATED_WRAP_REGEX, "$1$2")
        .replace(TABS_AND_SPACES_REGEX, " ")
        .replace(THREE_PLUS_NEWLINES_REGEX, "\n\n")
        .lines()
        .map(::normalizeLine)
        .filter(String::isNotBlank)
        .let(::mergeWrappedLines)
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

    private fun decodeBestEffort(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        return TEXT_CHARSETS
            .mapNotNull { charset -> runCatching { decode(bytes, charset) }.getOrNull() }
            .maxByOrNull(::decodedTextScore)
            .orEmpty()
    }

    private fun decode(bytes: ByteArray, charset: Charset): String {
        val decoder = charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return try {
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            ""
        }
    }

    private fun decodedTextScore(value: String): Int {
        if (value.isBlank()) return Int.MIN_VALUE
        return value.sumOf { char ->
            when {
                char == '\uFFFD' -> -6
                char.isLetterOrDigit() -> 3
                char.isWhitespace() -> 1
                char in listOf('.', ',', ':', ';', '!', '?', '-', '«', '»', '"', '(', ')') -> 1
                else -> -1
            }
        }
    }

    private fun normalizeLine(value: String): String = value
        .trim()
        .replace(EM_DASH_LINE_PREFIX_REGEX, "• ")
        .replace(NUMERIC_LIST_PREFIX_REGEX, "")
        .replace(MULTIPLE_BULLETS_REGEX, "• ")

    private fun mergeWrappedLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return emptyList()
        val merged = mutableListOf<String>()
        lines.forEach { line ->
            val previous = merged.lastOrNull()
            val shouldMerge = previous != null &&
                previous.lastOrNull()?.let { it.isLetterOrDigit() || it == ',' || it == ':' } == true &&
                line.firstOrNull()?.isLowerCase() == true &&
                !previous.startsWith("• ") &&
                !line.startsWith("• ")

            if (shouldMerge) {
                merged[merged.lastIndex] = "$previous $line"
            } else {
                merged += line
            }
        }
        return merged
    }

    private const val DOCX_DOCUMENT_XML = "word/document.xml"
    private val TEXT_NODE_REGEX = Regex("<w:t[^>]*>(.*?)</w:t>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val TABS_AND_SPACES_REGEX = Regex("[\\t\\x0B\\f ]+")
    private val THREE_PLUS_NEWLINES_REGEX = Regex("\\n{3,}")
    private val HYPHENATED_WRAP_REGEX = Regex("(\\p{L})-\\n(\\p{L})")
    private val HEADING_PREFIX_REGEX = Regex("^#{1,6}\\s*")
    private val BULLET_PREFIX_REGEX = Regex("^[-*+]\\s+")
    private val QUOTE_PREFIX_REGEX = Regex("^>\\s+")
    private val MARKDOWN_LINK_REGEX = Regex("\\[(.+?)]\\((.+?)\\)")
    private val MARKDOWN_IMAGE_REGEX = Regex("!\\[(.*?)]\\((.+?)\\)")
    private val MARKDOWN_CODE_REGEX = Regex("`([^`]+)`")
    private val EM_DASH_LINE_PREFIX_REGEX = Regex("^[—–]\\s+")
    private val NUMERIC_LIST_PREFIX_REGEX = Regex("^\\d+[.)]\\s+")
    private val MULTIPLE_BULLETS_REGEX = Regex("^[•·]\\s+")
    private val TEXT_CHARSETS = listOf(
        Charsets.UTF_8,
        Charsets.UTF_16,
        Charsets.UTF_16LE,
        Charsets.UTF_16BE,
        Charset.forName("windows-1251")
    )
}
