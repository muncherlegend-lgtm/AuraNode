package com.example.newapp.data.source

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.PackGenerationResult
import com.example.newapp.data.model.PackGenerationSource
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratedPackMerger @Inject constructor() {

    fun mergeWithOfflineFallback(
        cloudPack: QuizPack?,
        offlinePack: QuizPack,
        document: ImportedDocument,
        questionsPerDifficulty: Int,
        cloudFailure: Throwable? = null
    ): PackGenerationResult {
        if (cloudPack == null) {
            return PackGenerationResult(
                pack = offlinePack.copy(
                    description = "Оффлайн-черновик по материалу: ${document.displayName}"
                ),
                usedCloudGeneration = false,
                fallbackReason = cloudFailure?.message
            )
        }

        val mergedQuestions = buildList {
            Difficulty.entries.forEach { difficulty ->
                val cloudQuestions = cloudPack.questions.filter { it.difficulty == difficulty }
                val fallbackQuestions = offlinePack.questions.filter { it.difficulty == difficulty }
                addAll(
                    (cloudQuestions + fallbackQuestions)
                        .distinctBy { questionSignature(it) }
                        .take(questionsPerDifficulty)
                )
            }
        }

        val warnings = buildList {
            if (cloudFailure != null) {
                add(cloudFailure.message ?: "Cloud generation failed, fallback was used.")
            }
            if (mergedQuestions.size < offlinePack.questions.size) {
                add("Часть вопросов была дополнена локальной генерацией.")
            }
        }

        val usedCloud = mergedQuestions.any { merged ->
            cloudPack.questions.any { questionSignature(it) == questionSignature(merged) }
        }

        val completedQuestions = if (mergedQuestions.size < offlinePack.questions.size) {
            (mergedQuestions + offlinePack.questions)
                .distinctBy(::questionSignature)
                .take(offlinePack.questions.size)
        } else {
            mergedQuestions
        }

        return PackGenerationResult(
            pack = cloudPack.copy(
                generationSource = if (usedCloud) {
                    PackGenerationSource.CLOUD_AI
                } else {
                    PackGenerationSource.OFFLINE_DRAFT
                },
                questions = completedQuestions
            ),
            usedCloudGeneration = usedCloud,
            warnings = warnings,
            fallbackReason = if (usedCloud) null else cloudFailure?.message
        )
    }

    private fun questionSignature(question: Question): String =
        "${question.difficulty.name}|${question.text.trim()}|${question.options.joinToString("|")}"
}
