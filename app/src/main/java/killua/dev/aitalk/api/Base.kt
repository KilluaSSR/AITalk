package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface BaseApiConfig {
    val systemInstruction: String
    val floatingWindowSystemInstruction: String?
    val temperature: Double
}
data class OpenAIConfig(
    override val temperature: Double = 1.0,
    val topP: Double = 1.0,
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
) : BaseApiConfig

interface OpenAIApiService {
    fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        openAIConfig: OpenAIConfig
    ): Flow<String>
}

data class DeepSeekConfig(
    override val temperature: Double = 1.0,
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
): BaseApiConfig

interface DeepSeekApiService {
    fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        deepSeekConfig: DeepSeekConfig
    ): Flow<String>
}

data class GeminiConfig(
    override val temperature: Double = 1.0,
    val topP: Double = 0.95,
    val topK: Int = 40,
    val responseMimeType: String = "text/plain",
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
): BaseApiConfig

interface GeminiApiService {
    fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        geminiConfig: GeminiConfig
    ): Flow<String>
}

data class GrokConfig(
    override val temperature: Double = 0.0,
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
): BaseApiConfig

interface GrokApiService {
    fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        grokConfig: GrokConfig
    ): Flow<String>
}

abstract class BaseApiServiceImpl<C : BaseApiConfig>(
    protected val httpClient: OkHttpClient,
    private val apiName: String
) {
    abstract fun buildRequest(model: SubModel, prompt: String, apiKey: String, config: C, stream: Boolean): Request
    abstract fun parseStreamChunk(chunk: String): String?
    abstract fun parseSuccessfulResponse(responseBody: String): String

    fun getSystemInstruction(config: C): String {
        return (config.floatingWindowSystemInstruction ?: config.systemInstruction).trim()
    }

    suspend fun executeApiCall(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: C
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest(model, prompt, apiKey, config, stream = false)
            Log.d(apiName, "请求 URL: ${request.url}")


            httpClient.newCall(request).execute().use { response ->
                Log.d(apiName, "响应代码: ${response.code}")

                val responseBodyString = response.body?.string().orEmpty()
                Log.d(apiName, "原始响应体: $responseBodyString")

                if (!response.isSuccessful) {
                    Log.e(apiName, "HTTP 错误: ${response.code}, 错误体: $responseBodyString")
                    return@withContext Result.failure(IOException("HTTP Error ${response.code}: $responseBodyString"))
                }

                val text = parseSuccessfulResponse(responseBodyString)

                if (text.isEmpty()) {
                    Log.w(apiName, "未能从响应中提取文本，返回原始响应体。")
                    return@withContext Result.success(responseBodyString)
                }

                Log.d(apiName, "提取到的文本: $text")
                Result.success(text)
            }
        } catch (e: Exception) {
            Log.e(apiName, "$apiName 调用失败: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun executeStreamApiCall(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: C
    ): Flow<String> = flow {
        val request = buildRequest(model, prompt, apiKey, config, stream = true)
        Log.d(apiName, "流式请求 URL: ${request.url}")

        var response: Response? = null
        try {
            response = httpClient.newCall(request).execute()
            Log.d(apiName, "流式响应代码: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                response.close()
                Log.e(apiName, "HTTP 错误: ${response.code}, 错误体: $errorBody")
                throw IOException("HTTP Error ${response.code}: $errorBody")
            }

            val source = response.body?.source() ?: throw IOException("Response body is null")

            while (coroutineContext.isActive && !source.exhausted()) {
                val line = source.readUtf8Line()
                if (line.isNullOrBlank() || line.startsWith(":")) {
                    continue
                }

                if (line.startsWith("data: [DONE]")) {
                    break // Stream finished
                }

                if (line.startsWith("data: ")) {
                    val jsonData = line.substring(6)
                    parseStreamChunk(jsonData)?.let { contentChunk ->
                        emit(contentChunk)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(apiName, "$apiName 流式调用失败: ${e.message}", e)
            throw e
        } finally {
            response?.close()
        }
    }
}