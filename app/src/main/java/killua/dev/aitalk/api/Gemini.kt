package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.consts.GEMINI_URL
import killua.dev.aitalk.models.SubModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class GeminiApiServiceImpl @Inject constructor(
    httpClient: OkHttpClient
) : BaseApiServiceImpl<GeminiConfig>(httpClient, "GeminiAPI"), GeminiApiService {

    override suspend fun generateContent(
        model: SubModel,
        prompt: String,
        apiKey: String,
        geminiConfig: GeminiConfig
    ): Result<String> {
        return executeApiCall(model, prompt, apiKey, geminiConfig)
    }

    override fun buildRequest(model: SubModel, prompt: String, apiKey: String, config: GeminiConfig): Request {
        val url = GEMINI_URL+"${model.displayName.lowercase()}:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            put("generationConfig", JSONObject().apply {
                put("temperature", config.temperature)
                put("topP", config.topP)
                put("topK", config.topK)
                put("responseMimeType", config.responseMimeType)
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", getSystemInstruction(config)))
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt.trim()))
                    })
                })
            })
        }.toString()

        Log.d("GeminiAPI", "请求 JSON Payload: $requestBodyJson")

        return Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    override fun parseSuccessfulResponse(responseBody: String): String {
        return try {
            JSONObject(responseBody)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text", "")
                ?: ""
        } catch (e: Exception) {
            Log.e("GeminiAPI", "解析响应失败: $e")
            ""
        }
    }
}