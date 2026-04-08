package com.example.newapp.data.source

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackType
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizPackSerializationTest {

    @Test
    fun serializeAndParseQuizPackRoundTrips() {
        val pack = QuizPack(
            id = "pack_custom",
            title = "Custom Pack",
            description = "Generated from file",
            type = QuizPackType.CUSTOM_IMPORTED,
            generationSource = PackGenerationSource.CLOUD_AI,
            sourceFileName = "notes.md",
            sourceMimeType = "text/markdown",
            coverFact = "Cover fact",
            questions = listOf(
                Question(
                    id = 1,
                    text = "Question",
                    options = listOf("A", "B", "C", "D"),
                    correctAnswerIndex = 2,
                    difficulty = Difficulty.CADET,
                    explanation = "Explanation"
                )
            )
        )

        val serialized = QuizConfigParser.serializeQuizPack(pack)
        val parsed = QuizConfigParser.parseQuizPack(serialized)

        assertEquals(pack.id, parsed.id)
        assertEquals(pack.title, parsed.title)
        assertEquals(pack.type, parsed.type)
        assertEquals(pack.generationSource, parsed.generationSource)
        assertEquals(pack.sourceFileName, parsed.sourceFileName)
        assertEquals(1, parsed.questions.size)
        assertEquals("Question", parsed.questions.single().text)
    }
}
