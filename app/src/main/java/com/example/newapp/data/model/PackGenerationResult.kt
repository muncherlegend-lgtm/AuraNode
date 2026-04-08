package com.example.newapp.data.model

data class PackGenerationResult(
    val pack: QuizPack,
    val usedCloudGeneration: Boolean,
    val warnings: List<String> = emptyList(),
    val fallbackReason: String? = null
)
