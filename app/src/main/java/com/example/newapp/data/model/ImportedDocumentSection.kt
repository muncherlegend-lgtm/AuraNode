package com.example.newapp.data.model

data class ImportedDocumentSection(
    val id: String,
    val title: String,
    val text: String,
    val included: Boolean = true
)
