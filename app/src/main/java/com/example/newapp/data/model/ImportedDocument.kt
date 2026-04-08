package com.example.newapp.data.model

data class ImportedDocument(
    val displayName: String,
    val mimeType: String,
    val sourceExtension: String,
    val extractedText: String,
    val previewExcerpt: String,
    val estimatedQuestionCount: Int
)
