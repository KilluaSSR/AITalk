package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

interface BaseApiConfig {
    val systemInstruction: String
    val floatingWindowSystemInstruction: String?
    val temperature: Double
}

data class DeepSeekConfig(
    override val temperature: Double = 1.0,
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
): BaseApiConfig

interface DeepSeekApiService {
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        deepSeekConfig: DeepSeekConfig
    ): Result<String>
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
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        geminiConfig: GeminiConfig
    ): Result<String>
}

data class GrokConfig(
    override val temperature: Double = 0.0,
    override val systemInstruction: String = "You are a helpful assistant.",
    override val floatingWindowSystemInstruction: String? = null
): BaseApiConfig

interface GrokApiService {
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        grokConfig: GrokConfig
    ): Result<String>
}

abstract class BaseApiServiceImpl<C : BaseApiConfig>(
    protected val httpClient: OkHttpClient,
    private val apiName: String
) {
    protected abstract fun buildRequest(model: SubModel, prompt: String, apiKey: String, config: C): Request

    protected abstract fun parseSuccessfulResponse(responseBody: String): String

    protected fun getSystemInstruction(config: C): String {
        return (config.floatingWindowSystemInstruction ?: config.systemInstruction).trim()
    }

    protected suspend fun executeApiCall(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: C
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest(model, prompt, apiKey, config)
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
}