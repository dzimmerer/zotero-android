package org.zotero.android.pdf.reader

data class GeminiChatMessage(
    val role: GeminiChatRole,
    val content: String,
    val timestampMs: Long = System.currentTimeMillis(),
)

enum class GeminiChatRole {
    User,
    Assistant,
}

enum class ChatModelProvider {
    Gemini,
    OpenRouter,
}

object GeminiChatModels {
    const val defaultGeminiModel = "gemini-3-flash-preview"
    const val defaultOpenRouterModel = "stepfun/step-3.5-flash:free"
    val geminiSupported = listOf(
        defaultGeminiModel,
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.5-pro",
        "gemini-flash-latest",
    )
    val openRouterSupported = listOf(
        defaultOpenRouterModel,
        "openai/gpt-oss-120b:free",
    )

    fun supported(provider: ChatModelProvider): List<String> {
        return when (provider) {
            ChatModelProvider.Gemini -> geminiSupported
            ChatModelProvider.OpenRouter -> openRouterSupported
        }
    }

    fun defaultModel(provider: ChatModelProvider): String {
        return when (provider) {
            ChatModelProvider.Gemini -> defaultGeminiModel
            ChatModelProvider.OpenRouter -> defaultOpenRouterModel
        }
    }
}
