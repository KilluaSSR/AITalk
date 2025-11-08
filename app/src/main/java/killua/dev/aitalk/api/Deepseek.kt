package killua.dev.aitalk.api

import android.util.Log
import killua.dev.aitalk.consts.DEEPSEEK_URL
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.utils.JsonResponseParser
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class DeepSeekApiServiceImpl @Inject constructor(
    httpClient: OkHttpClient
) : BaseApiServiceImpl<DeepSeekConfig>(httpClient, "DeepSeekAPI"), DeepSeekApiService {

    override fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        deepSeekConfig: DeepSeekConfig
    ): Flow<String> {
        return executeStreamApiCall(model, prompt, apiKey, deepSeekConfig)
    }

    override fun buildRequest(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: DeepSeekConfig,
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
            put("model", model.displayName.lowercase())
            put("messages", messages)
            put("stream", stream)
            put("temperature", config.temperature)
        }.toString()

        Log.d("DeepSeekAPI", "请求 JSON Payload: $requestBodyJson")

        return Request.Builder()
            .url(DEEPSEEK_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    override fun parseSuccessfulResponse(responseBody: String): String {
        return JsonResponseParser.parseDeepSeekResponse(responseBody)
    }

    override fun parseStreamChunk(chunk: String): String? {
        return JsonResponseParser.parseDeepSeekStreamChunk(chunk)
    }

}