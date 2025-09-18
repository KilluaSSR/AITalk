package killua.dev.aitalk.repository.impl

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import killua.dev.aitalk.api.DeepSeekApiService
import killua.dev.aitalk.api.DeepSeekConfig
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GeminiConfig
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.api.GrokConfig
import killua.dev.aitalk.api.OpenAIApiService
import killua.dev.aitalk.api.OpenAIConfig
import killua.dev.aitalk.db.ConversationDao
import killua.dev.aitalk.db.ConversationEntity
import killua.dev.aitalk.db.MessageDao
import killua.dev.aitalk.db.MessageEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.ChatMessage
import killua.dev.aitalk.models.ChatRole
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.ChatRepository
import killua.dev.aitalk.repository.ChatMessageWithId
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.prepareAiSearchData

/**
 * Initial implementation: converts full message history to a single prompt string per model.
 * Future enhancement: provider-native multi-turn message formatting.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val apiConfigRepository: ApiConfigRepository,
    private val openAI: OpenAIApiService,
    private val gemini: GeminiApiService,
    private val deepSeek: DeepSeekApiService,
    private val grok: GrokApiService,
) : ChatRepository {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = mutableMapOf<Long, Job>() // assistantMessageId -> Job

    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val listAdapter = moshi.adapter<List<String>>(listType)

    override suspend fun sendUserMessage(conversationId: Long?, text: String, models: List<AIModel>): Long {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val convId = conversationId ?: run {
                val json = listAdapter.toJson(models.map { it.name })
                conversationDao.insert(
                    ConversationEntity(
                        createdAt = now,
                        updatedAt = now,
                        modelSetJson = json,
                        firstUserMessagePreview = text.take(80)
                    )
                )
            }
            val baseOrdering = (messageDao.getMaxOrdering(convId) ?: 0L) + 1
            val userMessageId = messageDao.insert(
                MessageEntity(
                    conversationId = convId,
                    ordering = baseOrdering,
                    role = "user",
                    content = text,
                    status = "completed",
                    createdAt = now
                )
            )
            // Insert assistant placeholders & launch streaming per model
            var orderingCursor = baseOrdering
            models.forEach { model ->
                orderingCursor += 1
                val assistantId = messageDao.insert(
                    MessageEntity(
                        conversationId = convId,
                        ordering = orderingCursor,
                        role = "assistant",
                        model = model.name,
                        content = "",
                        status = "streaming",
                        createdAt = System.currentTimeMillis()
                    )
                )
                launchModelStream(convId, assistantId, model)
            }
            convId
        }
    }

    override fun observeConversation(conversationId: Long): Flow<List<ChatMessageWithId>> =
        messageDao.observeMessages(conversationId).map { list ->
            list.map { entity ->
                val msg = when (entity.role) {
                    "user" -> ChatMessage(ChatRole.User, entity.content, null, entity.revision)
                    "assistant" -> ChatMessage(ChatRole.Assistant, entity.content, entity.model?.let { AIModel.valueOf(it) })
                    "system" -> ChatMessage(ChatRole.System, entity.content, null)
                    else -> ChatMessage(ChatRole.User, entity.content)
                }
                ChatMessageWithId(entity.id, msg)
            }
        }

    override suspend fun editUserMessage(conversationId: Long, messageId: Long, newText: String, models: List<AIModel>) {
        withContext(Dispatchers.IO) {
            val msg = messageDao.getById(messageId) ?: return@withContext
            if (msg.role != "user") return@withContext
            // Delete all messages after this ordering (overwrite strategy)
            messageDao.deleteAfterOrdering(conversationId, msg.ordering)
            // Overwrite content & increment revision
            messageDao.updateContent(messageId, newText)
            // Regenerate for models
            sendUserMessage(conversationId, newText, models)
        }
    }

    override fun cancelActiveGenerations(conversationId: Long) {
        synchronized(activeJobs) {
            val ids = activeJobs.keys.toList()
            ids.forEach { id ->
                activeJobs[id]?.cancel()
                activeJobs.remove(id)
            }
        }
    }

    override fun cancelAssistantMessage(messageId: Long) {
        synchronized(activeJobs) {
            activeJobs[messageId]?.cancel()
            activeJobs.remove(messageId)
        }
        scope.launch { messageDao.deleteMessage(messageId) }
    }

    override fun retryAssistantMessage(messageId: Long) {
        scope.launch {
            val msg = messageDao.getById(messageId) ?: return@launch
            if (msg.role != "assistant" || msg.model == null) return@launch
            // reset content & status then relaunch
            messageDao.updateContent(messageId, "")
            messageDao.updateStatus(messageId, "streaming", null)
            val model = runCatching { AIModel.valueOf(msg.model) }.getOrNull() ?: return@launch
            launchModelStream(msg.conversationId, messageId, model)
        }
    }

    private fun launchModelStream(conversationId: Long, assistantMessageId: Long, model: AIModel) {
        val job = scope.launch {
            try {
                val contextPrompt = buildContextPrompt(conversationId)
                val (subModel, apiKey, config, flow) = prepareProviderCall(model, contextPrompt)
                var buffer = StringBuilder()
                var lastFlush = System.currentTimeMillis()
                flow.collect { delta ->
                    buffer.append(delta)
                    val now = System.currentTimeMillis()
                    if (buffer.length >= 50 || now - lastFlush > 120) {
                        val current = messageDao.getById(assistantMessageId) ?: return@collect
                        messageDao.updateContent(assistantMessageId, current.content + buffer.toString())
                        buffer = StringBuilder()
                        lastFlush = now
                    }
                }
                if (buffer.isNotEmpty()) {
                    val current = messageDao.getById(assistantMessageId)
                    if (current != null) {
                        messageDao.updateContent(assistantMessageId, current.content + buffer.toString())
                    }
                }
                messageDao.updateStatus(assistantMessageId, "completed")
                ensureConversationTitle(conversationId, assistantMessageId)
            } catch (e: Exception) {
                messageDao.updateStatus(assistantMessageId, "error", e.message)
            }
        }
        synchronized(activeJobs) { activeJobs[assistantMessageId] = job }
    }

    private suspend fun ensureConversationTitle(conversationId: Long, assistantMessageId: Long) {
        val convo = conversationDao.getById(conversationId) ?: return
        if (convo.title != null) return
        val firstUser = messageDao.getMessagesSnapshot(conversationId).firstOrNull { it.role == "user" } ?: return
        val generated = firstUser.content.lines().first().take(20).ifBlank { "对话" }
        conversationDao.updateTitle(conversationId, generated)
    }

    /**
     * Build context prompt with truncation strategy:
     *  - Keep last MAX_USER_ASSISTANT_PAIRS * 2 messages (excluding system)
     *  - Format as: "User: ...\nAssistant(ModelX): ...\n"
     *  - Future: replace with provider-native messages array.
     */
    private suspend fun buildContextPrompt(conversationId: Long): String = withContext(Dispatchers.IO) {
        val all = messageDao.getMessagesSnapshot(conversationId)
        val MAX_MESSAGES = 30
        val trimmed = if (all.size > MAX_MESSAGES) all.takeLast(MAX_MESSAGES) else all
        val sb = StringBuilder()
        trimmed.forEach { m ->
            when (m.role) {
                "user" -> sb.append("User: ").append(m.content).append('\n')
                "assistant" -> sb.append("Assistant(")
                    .append(m.model ?: "?")
                    .append("): ")
                    .append(m.content).append('\n')
                "system" -> sb.append("System: ").append(m.content).append('\n')
            }
        }
        sb.toString()
    }

    private suspend fun prepareProviderCall(
        model: AIModel,
        prompt: String
    ): ProviderCallBundle {
        // Acquire subModel & config
        val subModel = apiConfigRepository.getDefaultSubModelForModel(model).firstOrNullOrFallback(model)
        val apiKey = apiConfigRepository.getApiKeyForModel(model).firstOrEmpty()
        val configBundle = when (model) {
            AIModel.ChatGPT -> ProviderCallBundle(subModel, apiKey, apiConfigRepository.getOpenAIConfig().firstBlocking(), openAI.generateContentStream(subModel, prompt, apiKey, apiConfigRepository.getOpenAIConfig().firstBlocking()))
            AIModel.Gemini -> ProviderCallBundle(subModel, apiKey, apiConfigRepository.getGeminiConfig().firstBlocking(), gemini.generateContentStream(subModel, prompt, apiKey, apiConfigRepository.getGeminiConfig().firstBlocking()))
            AIModel.DeepSeek -> ProviderCallBundle(subModel, apiKey, apiConfigRepository.getDeepSeekConfig().firstBlocking(), deepSeek.generateContentStream(subModel, prompt, apiKey, apiConfigRepository.getDeepSeekConfig().firstBlocking()))
            AIModel.Grok -> ProviderCallBundle(subModel, apiKey, apiConfigRepository.getGrokConfig().firstBlocking(), grok.generateContentStream(subModel, prompt, apiKey, apiConfigRepository.getGrokConfig().firstBlocking()))
            AIModel.Claude -> error("Claude not yet implemented")
        }
        return configBundle
    }
}

// Helper bundle
private data class ProviderCallBundle(
    val subModel: SubModel,
    val apiKey: String,
    val config: Any,
    val flow: kotlinx.coroutines.flow.Flow<String>
)

// Extension helpers (blocking first() simplified placeholder) — should be refactored with proper suspend first().
private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstBlocking(): T = this.first()
private suspend fun kotlinx.coroutines.flow.Flow<String>.firstOrEmpty(): String = runCatching { this.first() }.getOrElse { "" }
private suspend fun kotlinx.coroutines.flow.Flow<SubModel?>.firstOrNullBlocking(): SubModel? = this.first()
private suspend fun kotlinx.coroutines.flow.Flow<SubModel?>.firstOrNullOrFallback(model: AIModel): SubModel =
    firstOrNullBlocking() ?: SubModel.entries.first { it.parent == model }
