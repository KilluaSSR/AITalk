package killua.dev.aitalk.utils

/**
 * API密钥验证工具类
 */
object ApiKeyValidator {

    /**
     * 验证API密钥是否有效
     * @param apiKey API密钥字符串
     * @return 如果密钥有效返回true，否则返回false
     */
    fun isValidApiKey(apiKey: String): Boolean {
        return apiKey.isNotBlank() &&
               apiKey.trim().isNotEmpty()
    }

    /**
     * 验证API密钥是否为空或空白
     * @param apiKey API密钥字符串
     * @return 如果密钥为空或空白返回true，否则返回false
     */
    fun isBlankApiKey(apiKey: String): Boolean {
        return apiKey.isBlank()
    }
}

/**
 * String的扩展函数，用于快速验证API密钥
 */
fun String?.isValidApiKey(): Boolean =
    this != null && ApiKeyValidator.isValidApiKey(this)

fun String?.isBlankApiKey(): Boolean =
    this == null || ApiKeyValidator.isBlankApiKey(this)