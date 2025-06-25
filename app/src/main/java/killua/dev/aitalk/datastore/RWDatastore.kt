package killua.dev.aitalk.datastore

import android.content.Context
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.ui.theme.ThemeMode

fun Context.readTheme() = readStoreString(THEME_MODE, defValue = ThemeMode.SYSTEM.name)
fun Context.readSecureHistory() = readStoreBoolean(key = SECURE_HISTORY, defValue = false)
fun Context.readFloatingWindowQuestionMode() = readStoreString(FLOATING_WINDOW_QUESTION_MODE, defValue = FloatingWindowQuestionMode.isThatTrueWithExplain.name)

suspend fun Context.writeTheme(theme: String) = saveStoreString(THEME_MODE, theme)
suspend fun Context.writeSecureMyHistory(set: Boolean) = saveStoreBoolean(SECURE_HISTORY, set)
suspend fun Context.writeFloatingWindowQuestionMode(mode: String) = saveStoreString(FLOATING_WINDOW_QUESTION_MODE, mode)