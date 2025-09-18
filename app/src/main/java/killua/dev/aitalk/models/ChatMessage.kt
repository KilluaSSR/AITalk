package killua.dev.aitalk.models

/** Domain chat message abstraction independent from persistence layer */
enum class ChatRole { System, User, Assistant }

data class ChatMessage(
    val role: ChatRole,
    val content: String,
    val model: AIModel? = null,
    val revision: Int = 0
)
