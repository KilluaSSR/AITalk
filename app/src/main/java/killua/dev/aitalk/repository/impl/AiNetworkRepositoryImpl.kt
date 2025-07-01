package killua.dev.aitalk.repository.impl

import android.util.Log
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val geminiApiService: GeminiApiService,
    private val apiConfigRepository: ApiConfigRepository,
) : AiNetworkRepository {
    override suspend fun fetchResponse(query: String, subModel: SubModel): AIResponseState {
        return when (subModel.parent) {
            AIModel.Gemini -> {
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
                    onFailure = {
                        AIResponseState(
                            status = ResponseStatus.Error,
                            errorMessage = it.message
                        )
                    }
                )
            }
            else -> {

                try {
                    val request = Request.Builder()
                        .url("https://api.example.com/${subModel.parent.name.lowercase()}?q=$query")
                        .build()
                    httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        AIResponseState(
                            status = ResponseStatus.Success,
                            content = body,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                } catch (e: IOException) {
                    AIResponseState(
                        status = ResponseStatus.Error,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}