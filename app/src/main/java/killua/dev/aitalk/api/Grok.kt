package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.consts.GROK_URL
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class GrokApiServiceImpl @Inject constructor(
    httpClient: OkHttpClient
) : BaseApiServiceImpl<GrokConfig>(httpClient, "GrokAPI"), GrokApiService {

    override fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        grokConfig: GrokConfig
    ): Flow<String> {
        return executeStreamApiCall(model, prompt, apiKey, grokConfig)
    }

    override fun buildRequest(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: GrokConfig,
        stream: Boolean
    ): Request {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", getSystemInstruction(config))
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt.trim())
            })
        }
        val requestBodyJson = JSONObject().apply {
            put("messages", messages)
            put("model", model.displayName)
            put("stream", stream)
            put("temperature", config.temperature)
        }.toString()

        Log.d("GrokAPI", "请求 JSON Payload: $requestBodyJson")

        return Request.Builder()
            .url(GROK_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    override fun parseSuccessfulResponse(responseBody: String): String {
        return try {
            JSONObject(responseBody)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content", "")
                ?: ""
        } catch (e: Exception) {
            Log.e("GrokAPI", "解析响应失败: $e")
            ""
        }
    }

    override fun parseStreamChunk(chunk: String): String? {
        return try {
            JSONObject(chunk)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("delta")
                ?.optString("content", null)
        } catch (e: Exception) {
            Log.e("GrokAPI", "解析流式块失败: $e")
            null
        }
    }
}