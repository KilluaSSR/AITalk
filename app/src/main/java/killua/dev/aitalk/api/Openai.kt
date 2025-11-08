package killua.dev.aitalk.api
import android.util.Log
import killua.dev.aitalk.consts.OPENAI_RESPONSES_URL
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.utils.JsonResponseParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OpenAIApiServiceImpl @Inject constructor(
    httpClient: OkHttpClient
) : BaseApiServiceImpl<OpenAIConfig>(httpClient, "OpenAI_API"), OpenAIApiService {

    override fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        openAIConfig: OpenAIConfig
    ): Flow<String> = flow {
        val streamingHttpClient = httpClient.newBuilder()
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        val request = buildRequest(model, prompt, apiKey, openAIConfig, stream = true)
        Log.d("OpenAI_API", "使用定制流逻辑处理 OpenAI 请求...")

        var response: Response? = null
        try {
            response = streamingHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                response.close()
                throw IOException("HTTP Error ${response.code}: $errorBody")
            }

            val source = response.body?.source() ?: throw IOException("Response body is null")

            while (coroutineContext.isActive && !source.exhausted()) {
                val line = source.readUtf8Line()
                if (line == "event: response.output_text.delta") {
                    val dataLine = source.readUtf8Line()
                    if (dataLine != null) {
                        parseStreamChunk(dataLine)?.let { contentChunk ->
                            emit(contentChunk)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OpenAI_API", "OpenAI 流式调用失败: ${e.message}", e)
            throw e
        } finally {
            response?.close()
        }
    }

    override fun buildRequest(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: OpenAIConfig,
        stream: Boolean
    ): Request {
        val requestBodyJson = JSONObject().apply {
            put("model", model.displayName)
            put("instructions", getSystemInstruction(config))
            put("input", prompt.trim())
            put("stream", stream)
            put("temperature", config.temperature)
            put("top_p", config.topP)
        }.toString()

        Log.d("OpenAI_API", "请求 JSON Payload: $requestBodyJson")

        return Request.Builder()
            .url(OPENAI_RESPONSES_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    override fun parseStreamChunk(chunk: String): String? {
        return JsonResponseParser.parseOpenAIStreamChunk(chunk)
    }

    override fun parseSuccessfulResponse(responseBody: String): String {
        return JsonResponseParser.parseOpenAIResponse(responseBody)
    }
}