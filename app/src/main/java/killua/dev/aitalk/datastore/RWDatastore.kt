package killua.dev.aitalk.datastore

import android.content.Context
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.models.SubModel

fun Context.readTheme() = readStoreString(THEME_MODE, defValue = ThemeMode.SYSTEM.name)
fun Context.readSecureHistory() = readStoreBoolean(key = SECURE_HISTORY, defValue = false)
fun Context.readFloatingWindowQuestionMode() = readStoreString(FLOATING_WINDOW_QUESTION_MODE, defValue = FloatingWindowQuestionMode.isThatTrueWithExplain.name)
fun Context.readApiKeyForSubModel(subModel: SubModel, defValue: String = "") =
    readStoreString(apiKeyKeyForSubModel(subModel), defValue)
suspend fun Context.writeTheme(theme: String) = saveStoreString(THEME_MODE, theme)
suspend fun Context.writeSecureMyHistory(set: Boolean) = saveStoreBoolean(SECURE_HISTORY, set)
suspend fun Context.writeFloatingWindowQuestionMode(mode: String) = saveStoreString(FLOATING_WINDOW_QUESTION_MODE, mode)
suspend fun Context.writeApiKeyForSubModel(subModel: SubModel, apiKey: String) =
    saveStoreString(apiKeyKeyForSubModel(subModel), apiKey)