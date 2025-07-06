package killua.dev.aitalk.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Gemini
@JsonClass(generateAdapter = true)
data class GeminiStreamChunk(
    @Json(name = "candidates")
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content")
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts")
    val parts: List<Part>?
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text")
    val text: String?
)