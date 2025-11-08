package killua.dev.aitalk.utils

/**
 * API相关常量对象，用于提取硬编码值
 */
object ApiConstants {

    // API URL常量
    const val OPENAI_URL = "https://api.openai.com/v1/responses"
    const val DEEPSEEK_URL = "https://api.deepseek.com/chat/completions"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    const val GROK_URL = "https://api.x.ai/v1/chat/completions"

    // 超时设置
    val STREAMING_TIMEOUT_SECONDS = 300L
    val DEFAULT_TIMEOUT_SECONDS = 60L

    // 日志标签
    const val TAG_OPENAI = "OpenAI_API"
    const val TAG_DEEPSEEK = "DeepSeekAPI"
    const val TAG_GEMINI = "GeminiAPI"
    const val TAG_GROK = "GrokAPI"

    // 请求头常量
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val BEARER_PREFIX = "Bearer "
    const val JSON_MEDIA_TYPE = "application/json"

    // 流式数据处理常量
    const val STREAM_PREFIX_DATA = "data: "
    const val STREAM_PREFIX_EVENT = "event: "
    const val STREAM_DONE_MARKER = "[DONE]"
    const val STREAM_OPENAI_EVENT_DELTA = "event: response.output_text.delta"

    // HTTP状态码常量
    const val HTTP_OK = 200
    const val HTTP_BAD_REQUEST = 400
    const val HTTP_UNAUTHORIZED = 401
    const val HTTP_FORBIDDEN = 403
    const val HTTP_NOT_FOUND = 404
    const val HTTP_TOO_MANY_REQUESTS = 429
    const val HTTP_INTERNAL_SERVER_ERROR = 500
    const val HTTP_SERVICE_UNAVAILABLE = 503
    const val HTTP_GATEWAY_TIMEOUT = 504

    // API模型相关常量
    object Models {
        const val OPENAI_GPT_4_1 = "gpt-4.1"
        const val OPENAI_GPT_4O = "gpt-4o"
        const val OPENAI_GPT_O1 = "gpt-o1"
        const val OPENAI_GPT_O3 = "gpt-o3"

        const val DEEPSEEK_CHAT = "deepseek-chat"
        const val DEEPSEEK_REASONER = "deepseek-reasoner"

        const val GEMINI_2_5_FLASH = "gemini-2.5-flash"
        const val GEMINI_2_5_PRO = "gemini-2.5-pro"

        const val GROK_3 = "grok-3"
        const val GROK_3_MINI = "grok-3-mini"
        const val GROK_3_MINI_FAST = "grok-3-mini-fast"
    }

    // JSON字段常量
    object JsonFields {
        // 通用字段
        const val MODEL = "model"
        const val TEMPERATURE = "temperature"
        const val STREAM = "stream"
        const val CHOICES = "choices"
        const val MESSAGE = "message"
        const val CONTENT = "content"
        const val DELTA = "delta"
        const val TEXT = "text"

        // OpenAI特有字段
        const val INSTRUCTIONS = "instructions"
        const val INPUT = "input"
        const val OUTPUT = "output"
        const val TOP_P = "top_p"

        // Gemini特有字段
        const val GENERATION_CONFIG = "generationConfig"
        const val SYSTEM_INSTRUCTION = "systemInstruction"
        const val PARTS = "parts"
        const val CANDIDATES = "candidates"
        const val TOP_K = "top_k"
        const val RESPONSE_MIME_TYPE = "responseMimeType"

        // 消息角色字段
        const val ROLE = "role"
        const val MESSAGES = "messages"
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
    }

    // 错误消息常量
    object ErrorMessages {
        const val API_KEY_INVALID = "API密钥无效或未设置，请检查配置"
        const val RESPONSE_BODY_NULL = "Response body is null"
        const val HTTP_ERROR_TEMPLATE = "HTTP Error %d: %s"
        const val NETWORK_ERROR_TEMPLATE = "网络请求失败: %s"
        const val JSON_PARSE_ERROR = "JSON解析失败"
        const val UNKNOWN_ERROR = "未知错误"
    }

    // 动画和UI常量
    object UI {
        const val ANIMATION_DURATION_MS = 500
        const val FADE_ANIMATION_DURATION_MS = 350
        const val CROSSFADE_ANIMATION_DURATION_MS = 500
        const val EXPAND_COLLAPSE_ANIMATION_DURATION_MS = 350
    }

    // 数据验证常量
    object Validation {
        const val MIN_API_KEY_LENGTH = 10
        const val MAX_PROMPT_LENGTH = 4000
        const val DEFAULT_TEMPERATURE = 1.0
        const val MIN_TEMPERATURE = 0.0
        const val MAX_TEMPERATURE = 2.0
    }
}