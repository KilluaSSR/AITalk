package killua.dev.aitalk.api.configuration

data class DeepSeekConfig(
    val temperature: Double = 1.0,
    val systemInstruction: String = "You are a helpful assistant."
)