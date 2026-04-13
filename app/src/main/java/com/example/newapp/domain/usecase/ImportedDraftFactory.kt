package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.ImportedDocumentDraft
import com.example.newapp.data.model.ImportedDocumentSection
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuestionDraft
import com.example.newapp.data.model.QuizPack
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ImportedDraftFactory @Inject constructor(
    private val offlineQuizPackGenerator: OfflineQuizPackGenerator
) {

    fun createDraft(document: ImportedDocument, questionsPerDifficulty: Int): ImportedDocumentDraft {
        val sections = splitIntoSections(document)
        val questions = createDraftQuestions(document, sections, questionsPerDifficulty)
        return ImportedDocumentDraft(
            document = document,
            title = document.displayName.substringBeforeLast('.').ifBlank { "Новый набор" },
            description = "Набор вопросов по материалу ${document.displayName}",
            sections = sections,
            questionDrafts = questions
        )
    }

    fun rebuildQuestions(
        draft: ImportedDocumentDraft,
        questionsPerDifficulty: Int
    ): List<QuestionDraft> = createDraftQuestions(
        document = draft.document,
        sections = draft.sections,
        questionsPerDifficulty = questionsPerDifficulty
    )

    fun buildPack(draft: ImportedDocumentDraft): QuizPack {
        val preparedQuestions = draft.questionDrafts.mapIndexedNotNull { index, questionDraft ->
            val sanitizedOptions = questionDraft.options
                .map(String::trim)
                .filter(String::isNotBlank)
            if (questionDraft.text.isBlank() || sanitizedOptions.size != 4) {
                null
            } else {
                Question(
                    id = index + 1,
                    text = questionDraft.text.trim(),
                    options = sanitizedOptions,
                    correctAnswerIndex = questionDraft.correctAnswerIndex.coerceIn(0, 3),
                    difficulty = questionDraft.difficulty,
                    explanation = questionDraft.explanation.trim().ifBlank {
                        "Материал: ${draft.document.displayName}"
                    },
                    acceptedAnswers = listOf(
                        sanitizedOptions[questionDraft.correctAnswerIndex.coerceIn(0, 3)]
                    ),
                    difficultyWeight = when (questionDraft.difficulty) {
                        Difficulty.CADET -> 1
                        Difficulty.ENGINEER -> 2
                        Difficulty.COSMONAUT -> 3
                    }
                )
            }
        }

        return QuizPack(
            id = "pack_${UUID.randomUUID()}",
            title = draft.title.trim().ifBlank { "Новый набор" },
            description = draft.description.trim().ifBlank {
                "Пользовательский набор по материалу ${draft.document.displayName}"
            },
            sourceFileName = draft.document.displayName,
            sourceMimeType = draft.document.mimeType,
            coverFact = draft.questionDrafts.firstOrNull()?.explanation.orEmpty(),
            questions = preparedQuestions,
            type = com.example.newapp.data.model.QuizPackType.CUSTOM_IMPORTED,
            generationSource = com.example.newapp.data.model.PackGenerationSource.OFFLINE_DRAFT
        )
    }

    private fun splitIntoSections(document: ImportedDocument): List<ImportedDocumentSection> {
        val lines = document.extractedText
            .lines()
            .map(String::trim)
            .filter(String::isNotBlank)

        if (lines.isEmpty()) {
            return listOf(
                ImportedDocumentSection(
                    id = "section_1",
                    title = "Материал",
                    text = document.extractedText.take(500)
                )
            )
        }

        val sections = mutableListOf<ImportedDocumentSection>()
        val currentLines = mutableListOf<String>()
        var currentTitle = "Раздел 1"
        var sectionIndex = 1

        fun flushSection() {
            if (currentLines.isEmpty()) return
            val text = currentLines.joinToString(separator = "\n").trim()
            sections += ImportedDocumentSection(
                id = "section_$sectionIndex",
                title = currentTitle,
                text = text
            )
            sectionIndex += 1
            currentLines.clear()
        }

        lines.forEach { line ->
            val isHeading = looksLikeHeading(line)
            val currentLength = currentLines.sumOf { it.length }
            if (isHeading && currentLines.isNotEmpty()) {
                flushSection()
                currentTitle = normalizeHeading(line, sectionIndex)
            } else if (currentLength > TARGET_SECTION_LENGTH && looksLikeSectionBreak(line)) {
                flushSection()
                currentTitle = "Раздел $sectionIndex"
                currentLines += line
            } else {
                if (isHeading && currentLines.isEmpty()) {
                    currentTitle = normalizeHeading(line, sectionIndex)
                } else {
                    currentLines += line
                }
            }
        }
        flushSection()

        return sections.ifEmpty {
            listOf(
                ImportedDocumentSection(
                    id = "section_1",
                    title = "Материал",
                    text = document.extractedText.take(500)
                )
            )
        }
    }

    private fun createDraftQuestions(
        document: ImportedDocument,
        sections: List<ImportedDocumentSection>,
        questionsPerDifficulty: Int
    ): List<QuestionDraft> {
        val includedText = sections.filter { it.included }.joinToString("\n\n") { it.text }
            .ifBlank { document.extractedText }
        val generatedPack = offlineQuizPackGenerator.generate(
            document = document.copy(extractedText = includedText),
            questionsPerDifficulty = questionsPerDifficulty
        )
        return generatedPack.questions.map { question ->
            question.toDraft()
        }
    }

    private fun Question.toDraft(): QuestionDraft = QuestionDraft(
        id = id.toString(),
        text = text,
        options = options.take(4).padToFourOptions(),
        correctAnswerIndex = correctAnswerIndex.coerceIn(0, 3),
        difficulty = difficulty,
        explanation = explanation
    )

    private fun List<String>.padToFourOptions(): List<String> {
        val prepared = take(4).toMutableList()
        while (prepared.size < 4) {
            prepared += "Вариант ${prepared.size + 1}"
        }
        return prepared
    }

    private fun looksLikeHeading(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.startsWith("#") ||
            trimmed.matches(Regex("^\\d+[.)].+")) ||
            (trimmed.length in 4..60 && trimmed.none { it == '.' || it == '?' || it == '!' })
    }

    private fun looksLikeSectionBreak(value: String): Boolean {
        val normalized = value.lowercase(Locale.getDefault())
        return normalized.startsWith("- ") ||
            normalized.startsWith("•") ||
            normalized.startsWith("—") ||
            normalized.startsWith("важно") ||
            normalized.startsWith("итог")
    }

    private fun normalizeHeading(value: String, sectionIndex: Int): String {
        val normalized = value
            .trim()
            .trimStart('#')
            .trim()
            .replace(Regex("^\\d+[.)]\\s*"), "")
            .ifBlank { "Раздел $sectionIndex" }
        return normalized.take(80)
    }

    private companion object {
        const val TARGET_SECTION_LENGTH = 380
    }
}
