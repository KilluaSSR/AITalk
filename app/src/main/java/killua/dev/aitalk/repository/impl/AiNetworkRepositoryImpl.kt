package killua.dev.aitalk.repository.impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.utils.mapCommonNetworkErrorToUserFriendlyMessage
import killua.dev.aitalk.utils.mapGeminiErrorToUserFriendlyMessage
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val geminiApiService: GeminiApiService,
    private val apiConfigRepository: ApiConfigRepository,
    @ApplicationContext private val context: Context
) : AiNetworkRepository {
    override suspend fun fetchResponse(query: String, subModel: SubModel): AIResponseState {
        return when (subModel.parent) {
            AIModel.Gemini -> {
                try {
                    Log.d("AI", "Fetching response from Gemini API")
                    Log.d("AI", "Query: $query, model: $subModel")
                    val result = geminiApiService.generateContent(
                        model = subModel,
                        prompt = query,
                        apiKey = apiConfigRepository.getApiKeyForModel(subModel.parent).first()
                    )
                    result.fold(
                        onSuccess = {
                            AIResponseState(
                                status = ResponseStatus.Success,
                                content = it,
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
                }catch (e: Exception) {
                    AIResponseState(
                        status = ResponseStatus.Error,
                        errorMessage = context.mapCommonNetworkErrorToUserFriendlyMessage(subModel.parent.name, e)
                    )
                }
            }

            else -> {
                AIResponseState(status = ResponseStatus.Error)
            }
        }
    }
}