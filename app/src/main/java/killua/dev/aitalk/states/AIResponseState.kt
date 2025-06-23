package killua.dev.aitalk.states

data class AIResponseState(
    val status: ResponseStatus = ResponseStatus.Idle,
    val content: String? = null,
    val timestamp: Long? = null,
    val errorMessage: String? = null
)