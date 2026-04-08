package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.QuizPackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineQuizPackGeneratorTest {

    private val generator = OfflineQuizPackGenerator()

    @Test
    fun generateBuildsThreeDifficultyBucketsWithFourOptions() {
        val document = ImportedDocument(
            displayName = "physics.txt",
            mimeType = "text/plain",
            sourceExtension = "txt",
            extractedText = """
                Барнаул расположен на Оби и является административным центром края.
                Бийск имеет статус наукограда и развивает исследовательские проекты.
                Чуйский тракт считается одной из самых известных дорог Сибири.
                Денисова пещера принесла региону мировую археологическую известность.
                Белокуриха известна как курорт с термальными источниками.
                Кулундинская степь формирует особый природный ландшафт региона.
                Колывань связана с камнерезным искусством и яшмовыми изделиями.
                Тигирекский заповедник охраняет редкие горные экосистемы.
            """.trimIndent(),
            previewExcerpt = "preview",
            estimatedQuestionCount = 12
        )

        val pack = generator.generate(document, questionsPerDifficulty = 5)

        assertEquals(QuizPackType.CUSTOM_IMPORTED, pack.type)
        assertEquals(PackGenerationSource.OFFLINE_DRAFT, pack.generationSource)
        assertEquals(15, pack.questions.size)
        Difficulty.entries.forEach { difficulty ->
            assertEquals(5, pack.questions.count { it.difficulty == difficulty })
        }
        assertTrue(pack.questions.all { it.options.size == 4 })
    }
}
