package killua.dev.aitalk.utils

import android.content.Context
import android.util.Log
import killua.dev.aitalk.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 统一错误处理工具类
 */
object ErrorUtils {

    private const val TAG = "ErrorUtils"

    /**
     * 通用的HTTP错误码映射
     */
    fun mapHttpErrorCode(context: Context, statusCode: Int, modelName: String, message: String? = null): String {
        return when (statusCode) {
            400 -> context.getString(R.string.error_bad_request)
            401 -> context.getString(R.string.error_unauthorized)
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            429 -> context.getString(R.string.error_rate_limited)
            500 -> context.getString(R.string.error_server_error)
            502 -> context.getString(R.string.error_bad_gateway)
            503 -> context.getString(R.string.error_service_unavailable)
            504 -> context.getString(R.string.error_gateway_timeout)
            else -> context.getString(R.string.error_unknown_http, statusCode, message ?: "未知错误")
        }
    }

    /**
     * 网络异常处理
     */
    fun mapNetworkError(context: Context, e: Throwable, modelName: String): String {
        return when (e) {
            is UnknownHostException -> context.getString(R.string.error_network_connection_failed)
            is SocketTimeoutException -> context.getString(R.string.error_network_timeout)
            is IOException -> {
                if (e.message?.contains("HTTP Error", ignoreCase = true) == true) {
                    val httpStatusCode = extractHttpStatusCode(e.message)
                    httpStatusCode?.let { code ->
                        mapHttpErrorCode(context, code, modelName, e.message)
                    } ?: context.getString(R.string.error_model_network_issue, modelName, e.message ?: "网络问题")
                } else {
                    context.getString(R.string.error_model_network_issue, modelName, e.message ?: "网络问题")
                }
            }
            else -> context.getString(R.string.error_model_unknown_issue, modelName, e.message ?: "未知错误")
        }
    }

    /**
     * 从错误消息中提取HTTP状态码
     */
    private fun extractHttpStatusCode(message: String?): Int? {
        return message?.substringAfter("HTTP Error ")?.substringBefore(":")?.trim()?.toIntOrNull()
    }

    /**
     * 记录错误日志
     */
    fun logError(tag: String, error: Throwable, additionalInfo: String? = null) {
        val message = buildString {
            append("Error occurred")
            additionalInfo?.let { append(": $it") }
        }
        Log.e(tag, message, error)
    }

    /**
     * 创建统一的API错误信息
     */
    fun createApiErrorMessage(context: Context, e: Throwable, apiName: String): String {
        logError(apiName, e, "API调用失败")
        return mapNetworkError(context, e, apiName)
    }
}