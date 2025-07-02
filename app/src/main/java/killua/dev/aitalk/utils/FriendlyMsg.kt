package killua.dev.aitalk.utils

import android.content.Context
import android.util.Log
import killua.dev.aitalk.R
import java.io.IOException

fun Context.mapGeminiErrorToUserFriendlyMessage(e: Throwable): String {
    return when {
        e is IOException && e.message?.contains("HTTP Error", ignoreCase = true) == true -> {
            val httpStatusCode = e.message?.substringAfter("HTTP Error ")?.substringBefore(":")?.trim()?.toIntOrNull()
            when (httpStatusCode) {
                400 -> {
                    if (e.message?.contains("INVALID_ARGUMENT", ignoreCase = true) == true) {
                        getString(R.string.gemini_error_400_invalid_argument)
                    } else if (e.message?.contains("FAILED_PRECONDITION", ignoreCase = true) == true) {
                        getString(R.string.gemini_error_400_failed_precondition)
                    }  else {
                        getString(R.string.gemini_error_400_general, httpStatusCode ?: 400)
                    }
                }
                403 -> getString(R.string.gemini_error_403)
                404 -> getString(R.string.gemini_error_404)
                429 -> getString(R.string.gemini_error_429)
                500 -> getString(R.string.gemini_error_500)
                503 -> getString(R.string.gemini_error_503)
                504 -> getString(R.string.gemini_error_504)
                else -> getString(R.string.gemini_error_http_unknown, httpStatusCode ?: -1, e.message ?: "未知错误")
            }
        }
        e is IOException && (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true) ->
            getString(R.string.error_network_connection_failed)
        else -> getString(R.string.gemini_error_unknown, e.message ?: "未知错误")
    }
}

fun Context.mapGrokErrorToUserFriendlyMessage(e: Throwable): String {
    return when {
        e is IOException && e.message?.contains("HTTP Error", ignoreCase = true) == true -> {
            val httpStatusCode = e.message?.substringAfter("HTTP Error ")?.substringBefore(":")?.trim()?.toIntOrNull()
            when (httpStatusCode) {
                400 -> getString(R.string.grok_error_400)
                401 -> getString(R.string.grok_error_401)
                403 -> getString(R.string.grok_error_403)
                404 -> getString(R.string.grok_error_404)
                405 -> getString(R.string.grok_error_405)
                415 -> getString(R.string.grok_error_415)
                422 -> getString(R.string.grok_error_422)
                429 -> getString(R.string.grok_error_429)
                202 -> getString(R.string.grok_error_202) // 尽管是 2XX，但文档中列为“错误代码”
                else -> getString(R.string.grok_error_http_unknown, httpStatusCode ?: -1, e.message ?: "未知错误")
            }
        }
        e is IOException && (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true) ->
            getString(R.string.error_network_connection_failed)
        else -> getString(R.string.grok_error_unknown, e.message ?: "未知错误")
    }
}

fun Context.mapDeepSeekErrorToUserFriendlyMessage(e: Throwable): String {
    Log.e("DeepSeekErrorMapper", "映射 DeepSeek 错误: ${e.message}", e)
    return when {
        e is IOException && e.message?.contains("HTTP Error", ignoreCase = true) == true -> {
            val httpStatusCode = e.message?.substringAfter("HTTP Error ")?.substringBefore(":")?.trim()?.toIntOrNull()
            when (httpStatusCode) {
                400 -> getString(R.string.deepseek_error_400)
                401 -> getString(R.string.deepseek_error_401)
                402 -> getString(R.string.deepseek_error_402)
                422 -> getString(R.string.deepseek_error_422)
                429 -> getString(R.string.deepseek_error_429)
                500 -> getString(R.string.deepseek_error_500)
                503 -> getString(R.string.deepseek_error_503)
                else -> getString(R.string.deepseek_error_http_unknown, httpStatusCode ?: -1, e.message ?: "未知错误")
            }
        }
        e is IOException && (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true) ->
            getString(R.string.error_network_connection_failed)
        else -> getString(R.string.deepseek_error_unknown, e.message ?: "未知错误")
    }
}

fun Context.mapCommonNetworkErrorToUserFriendlyMessage(modelName: String, e: Throwable): String {
    return when {
        e is IOException && (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("connect", ignoreCase = true) == true) ->
            getString(R.string.error_network_connection_failed)
        e is IOException -> getString(R.string.error_model_network_issue, modelName, e.message ?: "未知网络问题")
        else -> getString(R.string.error_model_unknown_issue, modelName, e.message ?: "未知错误")
    }
}