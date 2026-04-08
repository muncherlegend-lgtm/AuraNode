package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test

class AtlasUnlockUseCaseTest {

    private val useCase = AtlasUnlockUseCase()

    @Test
    fun returnsAtlasNodeForCorrectAnswer() {
        val unlocked = useCase.unlockForQuestion(
            question = question(locationId = "barnaul"),
            isCorrect = true
        )

        assertEquals(setOf("barnaul"), unlocked)
    }

    @Test
    fun returnsEmptySetWhenAnswerIsWrongOrQuestionHasNoLocation() {
        assertEquals(
            emptySet<String>(),
            useCase.unlockForQuestion(question(locationId = "barnaul"), isCorrect = false)
        )
        assertEquals(
            emptySet<String>(),
            useCase.unlockForQuestion(question(locationId = ""), isCorrect = true)
        )
    }

    private fun question(locationId: String): Question = Question(
        id = 1,
        text = "Question",
        options = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 0,
        difficulty = Difficulty.CADET,
        explanation = "Explanation",
        locationId = locationId
    )
}
