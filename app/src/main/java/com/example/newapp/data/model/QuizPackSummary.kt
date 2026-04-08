package com.example.newapp.data.model

data class QuizPackSummary(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: QuizPackType,
    val generationSource: PackGenerationSource,
    val sourceFileName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val questionCount: Int = 0,
    val coverFact: String = ""
)
