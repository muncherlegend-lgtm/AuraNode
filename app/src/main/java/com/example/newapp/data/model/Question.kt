package com.example.newapp.data.model

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val difficulty: Difficulty,
    val explanation: String,
    val acceptedAnswers: List<String> = emptyList(),
    val locationId: String = "",
    val factCategory: FactCategory = FactCategory.HISTORY,
    val isLegendary: Boolean = false,
    val difficultyWeight: Int = 1,
    val unlockReward: String = ""
)
