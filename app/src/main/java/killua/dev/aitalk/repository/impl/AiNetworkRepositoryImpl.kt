package killua.dev.aitalk.repository.impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.mapCommonNetworkErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGeminiErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGrokErrorToUserFriendlyMessage
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val geminiApiService: GeminiApiService,
    private val grokApiService: GrokApiService,
    private val apiConfigRepository: ApiConfigRepository,
    @ApplicationContext private val context: Context
) : AiNetworkRepository {
    override suspend fun fetchResponse(query: String, subModel: SubModel): AIResponseState {
        val apiKey = apiConfigRepository.getApiKeyForModel(subModel.parent).first()
        return try {
            when (subModel.parent) {
                AIModel.Gemini -> {
                    geminiApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey
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
                    val systemMessage = apiConfigRepository.getGrokSystemMessage().first()
                    val temperature = apiConfigRepository.getGrokTemperature().first()

                    grokApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        systemMessage = systemMessage,
                        temperature = temperature
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
                else -> {
                    //占位
                    val systemMessage = apiConfigRepository.getGrokSystemMessage().first()
                    val temperature = apiConfigRepository.getGrokTemperature().first()

                    grokApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiKey,
                        systemMessage = systemMessage,
                        temperature = temperature
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