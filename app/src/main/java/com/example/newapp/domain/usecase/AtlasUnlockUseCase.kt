package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Question
import javax.inject.Inject

class AtlasUnlockUseCase @Inject constructor() {

    fun unlockForQuestion(question: Question, isCorrect: Boolean): Set<String> {
        return if (isCorrect && question.locationId.isNotBlank()) {
            setOf(question.locationId)
        } else {
            emptySet()
        }
    }
}
