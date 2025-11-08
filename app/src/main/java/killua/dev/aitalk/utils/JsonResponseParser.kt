package killua.dev.aitalk.utils

import org.json.JSONObject

/**
 * 使用GsonParser的JSON响应解析工具
 * 用于优化API实现中的JSON解析，减少重复代码
 */
object JsonResponseParser {

    /**
     * 解析OpenAI格式的响应
     */
    fun parseOpenAIResponse(responseBody: String): String {
        return try {
            // 使用GsonParser解析，失败时回退到JSONObject
            val responseMap = GsonParser.fromJsonMap<String, Any>(responseBody)
            if (responseMap != null) {
                val output = responseMap["output"] as? List<*>
                if (output != null && output.isNotEmpty()) {
                    val firstOutput = output[0] as? Map<*, *>
                    val content = firstOutput?.get("content") as? List<*>
                    if (content != null && content.isNotEmpty()) {
                        val textContent = content[0] as? Map<*, *>
                        return textContent?.get("text") as? String ?: ""
                    }
                }
            }

            // 回退到JSONObject解析
            JSONObject(responseBody)
                .optJSONArray("output")
                ?.optJSONObject(0)
                ?.optJSONArray("content")
                ?.optJSONObject(0)
                ?.optString("text", "")
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 解析DeepSeek格式的响应
     */
    fun parseDeepSeekResponse(responseBody: String): String {
        return try {
            // 使用GsonParser解析
            val responseMap = GsonParser.fromJsonMap<String, Any>(responseBody)
            if (responseMap != null) {
                val choices = responseMap["choices"] as? List<*>
                if (choices != null && choices.isNotEmpty()) {
                    val firstChoice = choices[0] as? Map<*, *>
                    val message = firstChoice?.get("message") as? Map<*, *>
                    return message?.get("content") as? String ?: ""
                }
            }

            // 回退到JSONObject解析
            JSONObject(responseBody)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content", "")
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 解析Gemini格式的响应
     */
    fun parseGeminiResponse(responseBody: String): String {
        return try {
            // 使用GsonParser解析
            val responseMap = GsonParser.fromJsonMap<String, Any>(responseBody)
            if (responseMap != null) {
                val candidates = responseMap["candidates"] as? List<*>
                if (candidates != null && candidates.isNotEmpty()) {
                    val firstCandidate = candidates[0] as? Map<*, *>
                    val content = firstCandidate?.get("content") as? Map<*, *>
                    val parts = content?.get("parts") as? List<*>
                    if (parts != null && parts.isNotEmpty()) {
                        val firstPart = parts[0] as? Map<*, *>
                        return firstPart?.get("text") as? String ?: ""
                    }
                }
            }

            // 回退到JSONObject解析
            JSONObject(responseBody)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text", "")
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 解析Grok格式的响应（与DeepSeek格式相同）
     */
    fun parseGrokResponse(responseBody: String): String = parseDeepSeekResponse(responseBody)

    /**
     * 通用的流式数据解析 - OpenAI格式
     */
    fun parseOpenAIStreamChunk(chunk: String): String? {
        return try {
            if (chunk.startsWith("data: ")) {
                val jsonData = chunk.substring(6)
                val dataMap = GsonParser.fromJsonMap<String, Any>(jsonData)
                return dataMap?.get("delta") as? String
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 通用的流式数据解析 - DeepSeek/Grok格式
     */
    fun parseDeepSeekStreamChunk(chunk: String): String? {
        return try {
            val dataMap = GsonParser.fromJsonMap<String, Any>(chunk)
            if (dataMap != null) {
                val choices = dataMap["choices"] as? List<*>
                if (choices != null && choices.isNotEmpty()) {
                    val firstChoice = choices[0] as? Map<*, *>
                    val delta = firstChoice?.get("delta") as? Map<*, *>
                    return delta?.get("content") as? String
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 通用的流式数据解析 - Gemini格式
     */
    fun parseGeminiStreamChunk(chunk: String): String? {
        return try {
            val dataMap = GsonParser.fromJsonMap<String, Any>(chunk)
            if (dataMap != null) {
                val candidates = dataMap["candidates"] as? List<*>
                if (candidates != null && candidates.isNotEmpty()) {
                    val firstCandidate = candidates[0] as? Map<*, *>
                    val content = firstCandidate?.get("content") as? Map<*, *>
                    val parts = content?.get("parts") as? List<*>
                    if (parts != null && parts.isNotEmpty()) {
                        val firstPart = parts[0] as? Map<*, *>
                        return firstPart?.get("text") as? String
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 通用的流式数据解析 - Grok格式（与DeepSeek格式相同）
     */
    fun parseGrokStreamChunk(chunk: String): String? = parseDeepSeekStreamChunk(chunk)
}