package killua.dev.aitalk.utils

private val invalidChars = Regex("[\\\\/:*?\"<>|\\n\\r\\t]+")

/**
 * Sanitize a filename segment (without path separators):
 * - Remove or replace reserved characters (Windows / Android common)
 * - Trim and collapse whitespace
 * - Limit length (default 60)
 * - Provide fallback when empty
 */
fun sanitizeFilenameSegment(raw: String, maxLength: Int = 60, fallback: String = "prompt"): String {
    var cleaned = raw.trim()
        .replace(invalidChars, "_")
        .replace("\u0000", "_")
        .replace(Regex("\u202E|\u202D"), "") // remove bidi control chars
    if (cleaned.isBlank()) cleaned = fallback
    if (cleaned.length > maxLength) cleaned = cleaned.substring(0, maxLength)
    return cleaned
}

/**
 * Build a safe base filename using prompt + timestamp + optional model.
 */
fun buildBaseFileName(prompt: String, modelName: String? = null, timestamp: java.time.LocalDateTime = java.time.LocalDateTime.now()): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val safePrompt = sanitizeFilenameSegment(prompt)
    val time = timestamp.format(formatter)
    return if (modelName != null) "${safePrompt}_${modelName}_$time" else "${safePrompt}_$time"
}
