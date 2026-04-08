package com.example.newapp.data.model

data class QuizPack(
    val id: String,
    val title: String,
    val description: String,
    val type: QuizPackType,
    val generationSource: PackGenerationSource = PackGenerationSource.OFFICIAL,
    val sourceFileName: String = "",
    val sourceMimeType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val coverFact: String = "",
    val questions: List<Question> = emptyList()
) {
    fun toSummary(): QuizPackSummary = QuizPackSummary(
        id = id,
        title = title,
        subtitle = description,
        type = type,
        generationSource = generationSource,
        sourceFileName = sourceFileName,
        createdAt = createdAt,
        questionCount = questions.size,
        coverFact = coverFact.ifBlank { questions.firstOrNull()?.explanation.orEmpty() }
    )

    companion object {
        const val OFFICIAL_ALTAI_PACK_ID = "official_altai_expedition"
    }
}
