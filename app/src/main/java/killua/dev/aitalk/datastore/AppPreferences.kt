package killua.dev.aitalk.datastore

import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.ui.theme.ThemeMode

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val secureHistory: Boolean = false,
    val locale: String = "",
    val floatingWindowQuestionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain,
    val saveDir: String = ""
)