package killua.dev.aitalk.models

data class ExtraInformation(
    val floatingWindowSystemInstructions: Map<AIModel, String?> = emptyMap(),
)
