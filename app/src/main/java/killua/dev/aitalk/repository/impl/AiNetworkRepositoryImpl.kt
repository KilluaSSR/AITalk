package killua.dev.aitalk.repository.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.DeepSeekApiService
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.api.OpenAIApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.ExtraInformation
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.isValidApiKey
import killua.dev.aitalk.utils.mapDeepSeekErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGeminiErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGrokErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapOpenAIErrorToUserFriendlyMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val grokApiService: GrokApiService,
    private val deepSeekApiService: DeepSeekApiService,
    private val openAIApiService: OpenAIApiService,
    private val apiConfigRepository: ApiConfigRepository,
    @ApplicationContext private val context: Context
) : AiNetworkRepository {
    override fun fetchResponseStream(
        query: String,
        subModel: SubModel,
        extraInformation: ExtraInformation
    ): Flow<AIResponseState> = flow {
        emit(AIResponseState(status = ResponseStatus.Loading))

        val apiKey = apiConfigRepository.getApiKeyForModel(subModel.parent).first()

        // 验证API密钥
        if (!apiKey.isValidApiKey()) {
            emit(AIResponseState(
                status = ResponseStatus.Error,
                errorMessage = "API密钥无效或未设置，请检查配置"
            ))
            return@flow
        }

        val contentBuilder = StringBuilder()
        var finalState: AIResponseState? = null

        val (apiFlow, errorMapper) = when (subModel.parent) {
            AIModel.Gemini -> {
                val config = apiConfigRepository.getGeminiConfig().first().copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                val flow = geminiApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, geminiConfig = config)
                Pair(flow, context::mapGeminiErrorToUserFriendlyMessage)
            }
            AIModel.Grok -> {
                val config = apiConfigRepository.getGrokConfig().first().copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                val flow = grokApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, grokConfig = config)
                Pair(flow, context::mapGrokErrorToUserFriendlyMessage)
            }
            AIModel.ChatGPT -> {
                val config = apiConfigRepository.getOpenAIConfig().first().copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                val flow = openAIApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, openAIConfig = config)
                Pair(flow, context::mapOpenAIErrorToUserFriendlyMessage)
            }
            AIModel.DeepSeek -> {
                val config = apiConfigRepository.getDeepSeekConfig().first().copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                val flow = deepSeekApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, deepSeekConfig = config)
                Pair(flow, context::mapDeepSeekErrorToUserFriendlyMessage)
            }
            else -> {
                val config = apiConfigRepository.getDeepSeekConfig().first().copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                val flow = deepSeekApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, deepSeekConfig = config)
                Pair(flow, context::mapDeepSeekErrorToUserFriendlyMessage)
            }
        }

        apiFlow
            .catch { e ->
                finalState = AIResponseState(
                    status = ResponseStatus.Error,
                    errorMessage = errorMapper(e)
                )
            }
            .collect { chunk ->
                contentBuilder.append(chunk)
                finalState = AIResponseState(
                    status = ResponseStatus.Success,
                    content = contentBuilder.toString(),
                    timestamp = System.currentTimeMillis()
                )
                emit(finalState)
            }

        finalState?.let { emit(it) }
    }
}