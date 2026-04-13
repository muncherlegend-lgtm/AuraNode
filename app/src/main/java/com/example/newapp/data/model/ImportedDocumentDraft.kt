package com.example.newapp.data.model

data class ImportedDocumentDraft(
    val document: ImportedDocument,
    val title: String,
    val description: String,
    val sections: List<ImportedDocumentSection>,
    val questionDrafts: List<QuestionDraft>
) {
    val includedSections: List<ImportedDocumentSection>
        get() = sections.filter { it.included }

    val excludedSectionsCount: Int
        get() = sections.count { !it.included }

    val includedText: String
        get() = includedSections.joinToString(separator = "\n\n") { it.text }
}
