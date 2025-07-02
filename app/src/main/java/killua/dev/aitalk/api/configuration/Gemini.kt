package killua.dev.aitalk.api.configuration

data class GeminiConfig(
    val temperature: Double = 1.0,
    val topP: Double = 0.95,
    val topK: Int = 40,
    val responseMimeType: String = "text/plain",
    val systemInstruction: String = "You are a helpful assistant."
)