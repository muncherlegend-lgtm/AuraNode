package com.example.newapp.data.model

data class AiGenerationConfig(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val cloudGenerationEnabled: Boolean = true,
    val hasCloudConsent: Boolean = false,
    val geminiModel: String = DEFAULT_GEMINI_MODEL,
    val openRouterModel: String = ""
) {
    companion object {
        const val DEFAULT_GEMINI_MODEL = "gemini-3-flash-preview"
    }
}
