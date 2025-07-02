package killua.dev.aitalk.api

import killua.dev.aitalk.api.configuration.DeepSeekConfig
import killua.dev.aitalk.models.SubModel
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import android.util.Log
interface DeepSeekApiService {
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        deepSeekConfig: DeepSeekConfig
    ): Result<String>
}

class DeepSeekApiServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : DeepSeekApiService {
    override suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        deepSeekConfig: DeepSeekConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.deepseek.com/chat/completions"
            Log.d("DeepSeekAPI", "请求 URL: $url")

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", deepSeekConfig.systemInstruction.trim())
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt.trim())
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("model", model.displayName)
                put("messages", messages)
                put("stream", false)
                put("temperature", deepSeekConfig.temperature)
            }.toString()

            Log.d("DeepSeekAPI", "请求 JSON Payload: $requestBodyJson")

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                Log.d("DeepSeekAPI", "响应代码: ${response.code}")

                val responseBodyString = response.body?.string().orEmpty()
                Log.d("DeepSeekAPI", "原始响应体: $responseBodyString")

                if (!response.isSuccessful) {
                    Log.e("DeepSeekAPI", "HTTP 错误: ${response.code}, 错误体: $responseBodyString")
                    return@withContext Result.failure(IOException("HTTP Error ${response.code}: $responseBodyString"))
                }

                val jsonResponse = JSONObject(responseBodyString)
                val text = jsonResponse
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content", "")
                    ?: ""

                if (text.isEmpty()) {
                    Log.w("DeepSeekAPI", "未能从响应中提取 'text'，返回原始响应体。")
                    return@withContext Result.success(responseBodyString)
                }

                Log.d("DeepSeekAPI", "提取到的文本: $text")
                Result.success(text)
            }
        } catch (e: Exception) {
            Log.e("DeepSeekAPI", "DeepSeek API 调用失败: ${e.message}", e)
            Result.failure(e)
        }
    }
}