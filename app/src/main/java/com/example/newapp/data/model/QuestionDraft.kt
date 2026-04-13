package com.example.newapp.data.model

data class QuestionDraft(
    val id: String,
    val text: String,
    val options: List<String> = List(4) { "" },
    val correctAnswerIndex: Int = 0,
    val difficulty: Difficulty = Difficulty.CADET,
    val explanation: String = ""
)
