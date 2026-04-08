package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.FactCategory
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackType
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class OfflineQuizPackGenerator @Inject constructor() {

    fun generate(document: ImportedDocument, questionsPerDifficulty: Int): QuizPack {
        val candidates = buildCandidateFacts(document.extractedText)
        val answersPool = candidates.mapNotNull { extractAnswerCandidate(it) }.distinct()
        val questions = buildList {
            Difficulty.entries.forEachIndexed { difficultyIndex, difficulty ->
                val offset = difficultyIndex * 1000
                repeat(questionsPerDifficulty) { questionIndex ->
                    val candidate = candidates[(difficultyIndex * questionsPerDifficulty + questionIndex) % candidates.size]
                    add(
                        createQuestion(
                            id = offset + questionIndex + 1,
                            candidateFact = candidate,
                            difficulty = difficulty,
                            distractorPool = answersPool
                        )
                    )
                }
            }
        }

        val normalizedTitle = document.displayName.substringBeforeLast('.').ifBlank { "Imported Pack" }
        return QuizPack(
            id = "pack_${UUID.randomUUID()}",
            title = normalizedTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            description = "Оффлайн-черновик по материалу: ${document.displayName}",
            type = QuizPackType.CUSTOM_IMPORTED,
            generationSource = PackGenerationSource.OFFLINE_DRAFT,
            sourceFileName = document.displayName,
            sourceMimeType = document.mimeType,
            coverFact = candidates.firstOrNull().orEmpty(),
            questions = questions
        )
    }

    private fun createQuestion(
        id: Int,
        candidateFact: String,
        difficulty: Difficulty,
        distractorPool: List<String>
    ): Question {
        val correctAnswer = extractAnswerCandidate(candidateFact).ifBlank {
            candidateFact.split(' ').firstOrNull { it.length > 4 }.orEmpty()
        }
        val prompt = candidateFact.replace(correctAnswer, "_____")
        val questionText = when (difficulty) {
            Difficulty.CADET -> "Какой фрагмент пропущен в материале?\n$prompt"
            Difficulty.ENGINEER -> "Согласно материалу, что лучше всего дополняет утверждение?\n$prompt"
            Difficulty.COSMONAUT -> "Какое понятие из исходного файла точнее всего завершает фрагмент?\n$prompt"
        }
        val distractors = distractorPool
            .filter { it.isNotBlank() && !it.equals(correctAnswer, ignoreCase = true) }
            .distinct()
            .shuffled()
            .take(3)
            .toMutableList()

        while (distractors.size < 3) {
            distractors += fallbackDistractorFor(correctAnswer, distractors + correctAnswer)
        }

        val options = (distractors + correctAnswer).shuffled()
        return Question(
            id = id,
            text = questionText,
            options = options,
            correctAnswerIndex = options.indexOf(correctAnswer),
            difficulty = difficulty,
            explanation = candidateFact,
            acceptedAnswers = listOf(correctAnswer),
            factCategory = inferFactCategory(candidateFact),
            difficultyWeight = when (difficulty) {
                Difficulty.CADET -> 1
                Difficulty.ENGINEER -> 2
                Difficulty.COSMONAUT -> 3
            },
            unlockReward = "Открыт фрагмент материала"
        )
    }

    private fun buildCandidateFacts(rawText: String): List<String> {
        val sentences = rawText
            .split(SENTENCE_DELIMITER)
            .map(String::trim)
            .filter { it.length in 45..220 }
            .distinct()

        if (sentences.size >= 8) {
            return sentences
        }

        return rawText.lines()
            .map(String::trim)
            .filter { it.length in 45..220 }
            .distinct()
            .ifEmpty { listOf(rawText.take(180)) }
    }

    private fun extractAnswerCandidate(source: String): String {
        QUOTED_PHRASE.find(source)?.groupValues?.getOrNull(1)?.let { return it.trim() }
        YEAR_PATTERN.find(source)?.value?.let { return it }
        PROPER_NOUN_PATTERN.findAll(source)
            .map { it.value.trim() }
            .firstOrNull { it.split(' ').size in 1..4 && it.length >= 4 }
            ?.let { return it }
        return source.split(' ')
            .map { token -> token.trim(*PUNCTUATION_CHARS) }
            .firstOrNull { it.length >= 6 && !STOP_WORDS.contains(it.lowercase(Locale.getDefault())) }
            .orEmpty()
    }

    private fun fallbackDistractorFor(correctAnswer: String, usedAnswers: List<String>): String {
        val seed = correctAnswer.filter(Char::isLetterOrDigit).take(6).ifBlank { "term" }
        var candidateIndex = 1
        while (true) {
            val candidate = "$seed-$candidateIndex"
            if (usedAnswers.none { it.equals(candidate, ignoreCase = true) }) {
                return candidate
            }
            candidateIndex += 1
        }
    }

    private fun inferFactCategory(source: String): FactCategory {
        val normalized = source.lowercase(Locale.getDefault())
        return when {
            normalized.contains("город") || normalized.contains("регион") -> FactCategory.HISTORY
            normalized.contains("река") || normalized.contains("озеро") || normalized.contains("лес") -> FactCategory.NATURE
            normalized.contains("маршрут") || normalized.contains("дорога") || normalized.contains("пут") -> FactCategory.TRAVEL
            normalized.contains("завод") || normalized.contains("производ") -> FactCategory.INDUSTRY
            normalized.contains("наук") || normalized.contains("исслед") -> FactCategory.SCIENCE
            else -> FactCategory.CULTURE
        }
    }

    private companion object {
        val SENTENCE_DELIMITER = Regex("(?<=[.!?])\\s+")
        val QUOTED_PHRASE = Regex("[«\"]([^»\"]{3,80})[»\"]")
        val YEAR_PATTERN = Regex("\\b\\d{3,4}\\b")
        val PROPER_NOUN_PATTERN = Regex("\\b[А-ЯЁA-Z][а-яёa-zA-Z-]+(?:\\s+[А-ЯЁA-Z][а-яёa-zA-Z-]+){0,2}")
        val PUNCTUATION_CHARS = charArrayOf(',', '.', ';', ':', '!', '?', '"', '\'', '«', '»', '(', ')')
        val STOP_WORDS = setOf("который", "которая", "которые", "также", "этого", "этот", "материал", "файл")
    }
}
