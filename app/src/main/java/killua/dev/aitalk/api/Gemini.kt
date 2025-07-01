package killua.dev.aitalk.api

import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.displayName}:generateContent?key=$apiKey"
            val json = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": ${prompt.trim().let { "\"${it.replace("\"", "\\\"")}\"" }} }
                      ]
                    }
                  ]
                }
            """.trimIndent()
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(IOException("HTTP ${response.code}"))
                val body = response.body?.string().orEmpty()
                val text = Regex("\"text\"\\s*:\\s*\"([^\"]*)\"").find(body)?.groupValues?.getOrNull(1) ?: body
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}