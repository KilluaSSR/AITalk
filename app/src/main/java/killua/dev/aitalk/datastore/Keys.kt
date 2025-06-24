package killua.dev.aitalk.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val THEME_MODE = stringPreferencesKey("theme_mode")
val LOCALE_MODE = stringPreferencesKey("locale_mode")
val SECURE_HISTORY = booleanPreferencesKey("secure_history")