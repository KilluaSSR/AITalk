package killua.dev.aitalk.api

import android.util.Log
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

interface GeminiApiService {
    suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String
    ): Result<String>
}

class GeminiApiServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : GeminiApiService {
    override suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.displayName.lowercase()}:generateContent?key=$apiKey"
            Log.d("GeminiURL", url)
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt.trim())
                            })
                        })
                    })
                })
            }.toString()
            Log.d("GeminiAPI", "Request JSON Payload: $requestBodyJson")
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            httpClient.newCall(request).execute().use { response ->
                Log.d("GeminiAPI", "Response Code: ${response.code}")

                val responseBodyString = response.body?.string().orEmpty()
                Log.d("GeminiAPI", "Raw Response Body: $responseBodyString")

                if (!response.isSuccessful) {
                    Log.e("GeminiAPI", "HTTP Error: ${response.code}, Error Body: $responseBodyString")
                    return@withContext Result.failure(IOException("HTTP Error ${response.code}: $responseBodyString"))
                }

                // 使用 JSONObject 解析响应体，安全地提取文本
                val jsonResponse = JSONObject(responseBodyString)
                val text = jsonResponse
                    .optJSONArray("candidates") // 获取 candidates 数组
                    ?.optJSONObject(0)          // 获取第一个 candidate 对象
                    ?.optJSONObject("content")  // 获取 content 对象
                    ?.optJSONArray("parts")     // 获取 parts 数组
                    ?.optJSONObject(0)          // 获取第一个 part 对象
                    ?.optString("text", "")     // 获取 text 字段，如果不存在则返回空字符串
                    ?: "" // 如果链式调用中任何一步为 null，则返回空字符串

                if (text.isEmpty()) {
                    Log.w("GeminiAPI", "Could not extract 'text' from response, returning raw body.")
                    return@withContext Result.success(responseBodyString) // 如果无法提取，返回原始响应体
                }

                Log.d("GeminiAPI", "Extracted Text: $text")
                Result.success(text)
            }
        } catch (e: Exception) {
            Log.d("Exception", "GeminiAPICallingFailed: $e")
            Result.failure(e)
        }
    }
}