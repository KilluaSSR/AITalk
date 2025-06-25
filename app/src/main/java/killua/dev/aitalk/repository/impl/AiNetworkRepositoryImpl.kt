package killua.dev.aitalk.repository.impl

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class AiNetworkRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : AiNetworkRepository {
    override suspend fun fetchResponse(model: AIModel, query: String): AIResponseState {
        return try {
            val request = Request.Builder()
                .url("https://api.example.com/${model.name.lowercase()}?q=$query")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                AIResponseState(
                    status = ResponseStatus.Success,
                    content = body,
                    timestamp = System.currentTimeMillis(),
                    errorMessage = null
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