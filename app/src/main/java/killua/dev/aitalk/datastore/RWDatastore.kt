package killua.dev.aitalk.datastore

import android.content.Context
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.ui.theme.ThemeMode

fun Context.readTheme() = readStoreString(THEME_MODE, defValue = ThemeMode.SYSTEM.name)
fun Context.readSecureHistory() = readStoreBoolean(key = SECURE_HISTORY, defValue = false)
fun Context.readFloatingWindowQuestionMode() = readStoreString(FLOATING_WINDOW_QUESTION_MODE, defValue = FloatingWindowQuestionMode.isThatTrueWithExplain.name)
fun Context.readSaveDir(defValue: String = DEFAULT_SAVE_DIR) =
    readStoreString(SAVE_DIR_KEY, defValue)
fun Context.readApiKeyForModel(model: AIModel, defValue: String = "") =
    readStoreString(apiKeyKeyForModel(model), defValue)
suspend fun Context.writeTheme(theme: String) = saveStoreString(THEME_MODE, theme)
suspend fun Context.writeSecureMyHistory(set: Boolean) = saveStoreBoolean(SECURE_HISTORY, set)
suspend fun Context.writeFloatingWindowQuestionMode(mode: String) = saveStoreString(FLOATING_WINDOW_QUESTION_MODE, mode)
suspend fun Context.writeApiKeyForModel(model: AIModel, apiKey: String) =
    saveStoreString(apiKeyKeyForModel(model), apiKey)
suspend fun Context.writeSaveDir(dir: String) =
    saveStoreString(SAVE_DIR_KEY, dir)