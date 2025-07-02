package killua.dev.aitalk.api.configuration

data class GrokConfig(
    val temperature: Double = 0.0,
    val systemInstruction: String = "You are a helpful assistant.",
    val floatingWindowSystemInstruction: String? = null
)