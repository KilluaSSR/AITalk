package killua.dev.aitalk.repository

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendUserMessage(conversationId: Long?, text: String, models: List<AIModel>): Long
    fun observeConversation(conversationId: Long): Flow<List<ChatMessageWithId>>
    suspend fun editUserMessage(conversationId: Long, messageId: Long, newText: String, models: List<AIModel>)
    fun cancelActiveGenerations(conversationId: Long)
    fun cancelAssistantMessage(messageId: Long)
    fun retryAssistantMessage(messageId: Long)
}

data class ChatMessageWithId(
    val id: Long,
    val message: ChatMessage,
)
