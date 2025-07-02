package killua.dev.aitalk.repository.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.DeepSeekApiService
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.mapCommonNetworkErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapDeepSeekErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGeminiErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGrokErrorToUserFriendlyMessage
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val grokApiService: GrokApiService,
    private val deepSeekApiService: DeepSeekApiService,
    private val apiConfigRepository: ApiConfigRepository,
    @ApplicationContext private val context: Context
) : AiNetworkRepository {
    override suspend fun fetchResponse(query: String, subModel: SubModel): AIResponseState {
        val apiKey = apiConfigRepository.getApiKeyForModel(subModel.parent).first()
        return try {
            when (subModel.parent) {
                AIModel.Gemini -> {
                    val geminiConfig = apiConfigRepository.getGeminiConfig().first()
                    geminiApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        geminiConfig = geminiConfig
                    ).fold(
                        onSuccess = { content ->
                            AIResponseState(
                                status = ResponseStatus.Success,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            )
                        },
                        onFailure = { e ->
                            AIResponseState(
                                status = ResponseStatus.Error,
                                errorMessage = context.mapGeminiErrorToUserFriendlyMessage(e)
                            )
                        }
                    )
                }
                AIModel.Grok -> {
                    val grokConfig = apiConfigRepository.getGrokConfig().first()
                    grokApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        grokConfig = grokConfig
                    ).fold(
                        onSuccess = { content ->
                            AIResponseState(
                                status = ResponseStatus.Success,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            )
                        },
                        onFailure = { e ->
                            AIResponseState(
                                status = ResponseStatus.Error,
                                errorMessage = context.mapGrokErrorToUserFriendlyMessage(e)
                            )
                        }
                    )
                }
                AIModel.DeepSeek -> {
                    val deepSeekConfig = apiConfigRepository.getDeepSeekConfig().first()
                    deepSeekApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        deepSeekConfig = deepSeekConfig
                    ).fold(
                        onSuccess = { content ->
                            AIResponseState(
                                status = ResponseStatus.Success,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            )
                        },
                        onFailure = { e ->
                            AIResponseState(
                                status = ResponseStatus.Error,
                                errorMessage = context.mapDeepSeekErrorToUserFriendlyMessage(e) // 调用 DeepSeek 专属错误映射
                            )
                        }
                    )
                }
                else -> {
                    val grokConfig = apiConfigRepository.getGrokConfig().first()
                    grokApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        grokConfig = grokConfig
                    ).fold(
                        onSuccess = { content ->
                            AIResponseState(
                                status = ResponseStatus.Success,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            )
                        },
                        onFailure = { e ->
                            AIResponseState(
                                status = ResponseStatus.Error,
                                errorMessage = context.mapGrokErrorToUserFriendlyMessage(e)
                            )
                        }
                    )
                }
            }
        } catch (e: Exception) {
            AIResponseState(
                status = ResponseStatus.Error,
                errorMessage = context.mapCommonNetworkErrorToUserFriendlyMessage(subModel.parent.name, e)
            )
        }
    }
}