package killua.dev.aitalk.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import killua.dev.aitalk.models.SubModel

val THEME_MODE = stringPreferencesKey("theme_mode")
val LOCALE_MODE = stringPreferencesKey("locale_mode")
val SECURE_HISTORY = booleanPreferencesKey("secure_history")
val FLOATING_WINDOW_QUESTION_MODE = stringPreferencesKey("floating_window_question_mode")
val SAVE_DIR_KEY = stringPreferencesKey("save_dir")
fun apiKeyKeyForSubModel(subModel: SubModel): Preferences.Key<String> =
    stringPreferencesKey("api_key_${subModel.parent.name.lowercase()}_${subModel.name.lowercase()}")

