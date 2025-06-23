package killua.dev.aitalk.states

enum class ResponseStatus {
    Idle,      // 初始状态
    Loading,   // 请求中
    Success,   // 成功
    Error,     // 错误
    Timeout    // 超时
}