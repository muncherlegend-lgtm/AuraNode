package com.example.newapp.data.source

import com.example.newapp.data.model.AiGenerationConfig
import com.example.newapp.data.model.AiProvider
import com.example.newapp.data.model.ImportedDocument
import com.example.newapp.data.model.Question
import com.example.newapp.data.model.QuizPack
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.data.model.PackGenerationSource
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class CloudQuizPackGenerator @Inject constructor() {

    suspend fun generate(
        document: ImportedDocument,
        config: AiGenerationConfig,
        questionsPerDifficulty: Int
    ): QuizPack = withContext(Dispatchers.IO) {
        require(config.apiKey.isNotBlank()) { "API key is missing." }
        val prompt = buildPrompt(document, questionsPerDifficulty)
        val rawResponse = when (config.provider) {
            AiProvider.GEMINI -> callGemini(prompt, config)
            AiProvider.OPENROUTER -> callOpenRouter(prompt, config)
        }
        val payload = extractJsonPayload(rawResponse)
        val json = JSONObject(payload)
        val title = json.optString("title").ifBlank { document.displayName.substringBeforeLast('.') }
        val description = json.optString("description").ifBlank {
            "AI-сгенерированный набор по материалу: ${document.displayName}"
        }
        val coverFact = json.optString("coverFact")
        val questions = QuizConfigParser.parseQuestions(
            JSONObject().put("questions", json.optJSONArray("questions") ?: JSONArray()).toString()
        )
        QuizPack(
            id = "pack_${UUID.randomUUID()}",
            title = title,
            description = description,
            type = QuizPackType.CUSTOM_IMPORTED,
            generationSource = PackGenerationSource.CLOUD_AI,
            sourceFileName = document.displayName,
            sourceMimeType = document.mimeType,
            coverFact = coverFact,
            questions = questions
        )
    }

    private fun callGemini(prompt: String, config: AiGenerationConfig): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.geminiModel}:generateContent"
        val body = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.35)
                    .put("responseMimeType", "application/json")
            )
        }
        val response = executeJsonRequest(
            url = endpoint,
            requestBody = body.toString(),
            requestHeaders = mapOf(
                "Content-Type" to "application/json",
                "x-goog-api-key" to config.apiKey
            )
        )
        return JSONObject(response)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .let { parts ->
                buildString {
                    for (index in 0 until parts.length()) {
                        append(parts.getJSONObject(index).optString("text"))
                    }
                }
            }
    }

    private fun callOpenRouter(prompt: String, config: AiGenerationConfig): String {
        val body = JSONObject().apply {
            if (config.openRouterModel.isNotBlank()) {
                put("model", config.openRouterModel)
            }
            put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", "Return valid JSON only. Do not wrap JSON in markdown.")
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", prompt)
                    )
            )
            put("temperature", 0.35)
            put("response_format", JSONObject().put("type", "json_object"))
        }
        val response = executeJsonRequest(
            url = "https://openrouter.ai/api/v1/chat/completions",
            requestBody = body.toString(),
            requestHeaders = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${config.apiKey}",
                "X-Title" to "AuraNode"
            )
        )
        return JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .optString("content")
    }

    private fun executeJsonRequest(
        url: String,
        requestBody: String,
        requestHeaders: Map<String, String>
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 30_000
            doInput = true
            doOutput = true
            requestHeaders.forEach { (key, value) -> setRequestProperty(key, value) }
        }

        connection.outputStream.use { output ->
            output.write(requestBody.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        val responseText = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()

        check(responseCode in 200..299) { "Cloud generation failed: $responseCode $responseText" }
        return responseText
    }

    private fun extractJsonPayload(rawText: String): String {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        FENCED_JSON_REGEX.find(trimmed)?.groupValues?.getOrNull(1)?.let { fenced ->
            return fenced.trim()
        }

        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        require(start != -1 && end != -1 && end > start) { "Cloud response did not contain JSON." }
        return trimmed.substring(start, end + 1)
    }

    private fun buildPrompt(document: ImportedDocument, questionsPerDifficulty: Int): String = """
        You create a quiz pack from study material.
        Read the source text and return JSON only.

        Requirements:
        - language: Russian
        - 3 difficulties: CADET, ENGINEER, COSMONAUT
        - exactly $questionsPerDifficulty questions per difficulty
        - every question must have exactly 4 answer options
        - exactly 1 correct answer
        - explanation must be short and fact-based
        - acceptedAnswers should contain the correct option as text
        - locationId should be empty for generic imported packs
        - factCategory must be one of HISTORY, NATURE, SCIENCE, INDUSTRY, TRAVEL, CULTURE
        - isLegendary must be false
        - difficultyWeight: 1 for CADET, 2 for ENGINEER, 3 for COSMONAUT
        - unlockReward should be "Открыт фрагмент материала"

        Output schema:
        {
          "title": "short title",
          "description": "short description",
          "coverFact": "one concise fact",
          "questions": [
            {
              "id": 1,
              "text": "...",
              "options": ["...", "...", "...", "..."],
              "correctAnswerIndex": 0,
              "difficulty": "CADET",
              "explanation": "...",
              "acceptedAnswers": ["..."],
              "locationId": "",
              "factCategory": "HISTORY",
              "isLegendary": false,
              "difficultyWeight": 1,
              "unlockReward": "Открыт фрагмент материала"
            }
          ]
        }

        Source file: ${document.displayName}
        Source text:
        ${document.extractedText.take(MAX_PROMPT_TEXT)}
    """.trimIndent()

    private companion object {
        const val MAX_PROMPT_TEXT = 12_000
        val FENCED_JSON_REGEX = Regex("```(?:json)?\\s*(\\{.*})\\s*```", RegexOption.DOT_MATCHES_ALL)
    }
}
