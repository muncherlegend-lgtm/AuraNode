package com.example.newapp.data.source

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class QuizPackStorageDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun loadAllCustomPacks() = withContext(Dispatchers.IO) {
        packDirectory()
            .listFiles { file -> file.extension.equals("json", ignoreCase = true) }
            .orEmpty()
            .mapNotNull { file ->
                runCatching { QuizConfigParser.parseQuizPack(file.readText(Charsets.UTF_8)) }.getOrNull()
            }
            .sortedByDescending { it.createdAt }
    }

    suspend fun loadCustomPack(packId: String) = withContext(Dispatchers.IO) {
        val file = File(packDirectory(), "$packId.json")
        if (!file.exists()) {
            null
        } else {
            runCatching { QuizConfigParser.parseQuizPack(file.readText(Charsets.UTF_8)) }.getOrNull()
        }
    }

    suspend fun saveCustomPack(packJson: String, packId: String) = withContext(Dispatchers.IO) {
        val file = File(packDirectory(), "$packId.json")
        file.writeText(packJson, Charsets.UTF_8)
    }

    suspend fun deleteCustomPack(packId: String) = withContext(Dispatchers.IO) {
        File(packDirectory(), "$packId.json").delete()
    }

    private fun packDirectory(): File = File(context.filesDir, PACKS_DIRECTORY).apply {
        if (!exists()) mkdirs()
    }

    private companion object {
        const val PACKS_DIRECTORY = "quiz_packs"
    }
}
