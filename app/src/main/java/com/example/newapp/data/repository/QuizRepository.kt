package com.example.newapp.data.repository

import android.net.Uri
import com.example.newapp.data.model.Achievement
import com.example.newapp.data.model.AtlasNode
import com.example.newapp.data.model.CloudGenerationMode
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.ImportedDocumentDraft
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.PackGenerationResult
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuestionDraft
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackSummary
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizSettings
import com.example.newapp.data.model.RunSummary
import com.example.newapp.data.model.ThemePreset

interface QuizRepository {
    fun getQuestionsByDifficulty(
        difficulty: Difficulty,
        mode: QuizMode = QuizMode.CLASSIC,
        packId: String = QuizPack.OFFICIAL_ALTAI_PACK_ID
    ): List<Question>
    fun getQuizConfig(): QuizSettings
    suspend fun getQuizSettings(): QuizSettings
    fun getThemePresets(): List<ThemePreset>
    fun getAtlasNodes(): List<AtlasNode>
    fun getAchievements(): List<Achievement>
    suspend fun getQuizPacks(): List<QuizPackSummary>
    suspend fun getQuizPack(packId: String): QuizPack?
    suspend fun prepareImportedDocument(uri: Uri): ImportedDocument
    suspend fun generateImportedPack(
        document: ImportedDocument,
        mode: CloudGenerationMode
    ): PackGenerationResult
    suspend fun createImportedDraft(
        document: ImportedDocument,
        questionsPerDifficulty: Int
    ): ImportedDocumentDraft
    suspend fun rebuildImportedDraftQuestions(
        draft: ImportedDocumentDraft,
        questionsPerDifficulty: Int
    ): List<QuestionDraft>
    suspend fun saveImportedDraft(draft: ImportedDocumentDraft): QuizPack
    suspend fun deleteCustomPack(packId: String)
    suspend fun deleteAllCustomPacks()
    suspend fun getPlayerProgress(): PlayerProgress
    suspend fun saveSelectedTheme(themeId: String)
    suspend fun saveQuizSettings(settings: QuizSettings)
    suspend fun persistRunOutcome(summary: RunSummary): PlayerProgress
    suspend fun resetProgress()
}
