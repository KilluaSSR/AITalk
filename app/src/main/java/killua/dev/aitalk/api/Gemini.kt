package killua.dev.aitalk.api

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import killua.dev.aitalk.consts.GEMINI_URL
import killua.dev.aitalk.models.GeminiStreamChunk
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GeminiApiServiceImpl @Inject constructor(
    httpClient: OkHttpClient
) : BaseApiServiceImpl<GeminiConfig>(httpClient, "GeminiAPI"), GeminiApiService {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val chunkAdapter: JsonAdapter<GeminiStreamChunk> = moshi.adapter(GeminiStreamChunk::class.java)
    override fun generateContentStream(
        model: SubModel,
        prompt: String,
        apiKey: String,
        geminiConfig: GeminiConfig
    ): Flow<String> = flow {
        val streamingHttpClient = httpClient.newBuilder()
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        val request = buildRequest(model, prompt, apiKey, geminiConfig, stream = true)
        Log.d("GeminiAPI", "使用带长超时的客户端处理 Gemini 流...")

        var response: Response? = null
        try {
            response = streamingHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("HTTP Error ${response.code}: ${response.body?.string()}")
            }

            response.body?.source()?.use { source ->
                val reader = JsonReader.of(source)
                reader.beginArray()
                while (reader.hasNext()) {
                    val chunk = chunkAdapter.fromJson(reader)
                    val text = chunk?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        emit(text)
                    }
                }
                reader.endArray()
            }
        } catch (e: Exception) {
            Log.e("GeminiAPI", "Gemini 流式调用失败: ${e.message}", e)
            throw e
        } finally {
            response?.body?.close()
        }
    }

        override fun buildRequest(
        model: SubModel,
        prompt: String,
        apiKey: String,
        config: GeminiConfig,
        stream: Boolean
    ): Request {
        val action = if (stream) "streamGenerateContent" else "generateContent"
        val url = "${GEMINI_URL}${model.displayName.lowercase()}:$action?key=$apiKey"

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

        return Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    override fun parseStreamChunk(chunk: String): String? {
        return try {
            JSONObject(chunk)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text", null)
        } catch (e: Exception) {
            Log.e("GeminiAPI", "解析流式块失败: $e")
            null
        }
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