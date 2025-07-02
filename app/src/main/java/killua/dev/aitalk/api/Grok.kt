package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.api.configuration.GrokConfig
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

interface GrokApiService {
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        grokConfig: GrokConfig
    ): Result<String>
}

class GrokApiServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : GrokApiService {
    override suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        grokConfig: GrokConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.x.ai/v1/chat/completions"
            Log.d("GrokAPI", "请求 URL: $url")

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", (grokConfig.floatingWindowSystemInstruction ?: grokConfig.systemInstruction).trim())
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt.trim())
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("messages", messages)
                put("model", model.displayName)
                put("stream", false)
                put("temperature", grokConfig.temperature)
            }.toString()

            Log.d("GrokAPI", "请求 JSON Payload: $requestBodyJson")

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                Log.d("GrokAPI", "响应代码: ${response.code}")

                val responseBodyString = response.body?.string().orEmpty()
                Log.d("GrokAPI", "原始响应体: $responseBodyString")

                if (!response.isSuccessful) {
                    Log.e("GrokAPI", "HTTP 错误: ${response.code}, 错误体: $responseBodyString")
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
                    Log.w("GrokAPI", "未能从响应中提取 'text'，返回原始响应体。")
                    return@withContext Result.success(responseBodyString)
                }

                Log.d("GrokAPI", "提取到的文本: $text")
                Result.success(text)
            }
        } catch (e: Exception) {
            Log.e("GrokAPI", "Grok API 调用失败: ${e.message}", e)
            Result.failure(e)
        }
    }
}