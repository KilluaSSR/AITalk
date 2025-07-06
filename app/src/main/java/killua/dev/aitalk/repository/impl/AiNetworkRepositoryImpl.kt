package killua.dev.aitalk.repository.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.DeepSeekApiService
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.ExtraInformation
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.mapCommonNetworkErrorToUserFriendlyMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val grokApiService: GrokApiService,
    private val deepSeekApiService: DeepSeekApiService,
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
        val contentBuilder = StringBuilder()
        var finalState: AIResponseState? = null

        val apiFlow = when (subModel.parent) {
            AIModel.Gemini -> {
                val baseGeminiConfig = apiConfigRepository.getGeminiConfig().first()
                val geminiConfigWithFW = baseGeminiConfig.copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                geminiApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, geminiConfig = geminiConfigWithFW)
            }
            AIModel.Grok -> {
                val baseGrokConfig = apiConfigRepository.getGrokConfig().first()
                val grokConfigWithFW = baseGrokConfig.copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                grokApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, grokConfig = grokConfigWithFW)
            }
            else -> {
                val baseDeepSeekConfig = apiConfigRepository.getDeepSeekConfig().first()
                val deepSeekConfigWithFW = baseDeepSeekConfig.copy(
                    floatingWindowSystemInstruction = extraInformation.floatingWindowSystemInstructions[subModel.parent]
                )
                deepSeekApiService.generateContentStream(model = subModel, prompt = query, apiKey = apiKey, deepSeekConfig = deepSeekConfigWithFW)
            }
        }

        apiFlow
            .catch { e ->
                finalState = AIResponseState(
                    status = ResponseStatus.Error,
                    errorMessage = context.mapCommonNetworkErrorToUserFriendlyMessage(subModel.parent.name, e)
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