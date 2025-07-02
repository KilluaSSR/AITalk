package killua.dev.aitalk.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import killua.dev.aitalk.models.AIModel

val THEME_MODE = stringPreferencesKey("theme_mode")
val LOCALE_MODE = stringPreferencesKey("locale_mode")
val SECURE_HISTORY = booleanPreferencesKey("secure_history")
val FLOATING_WINDOW_QUESTION_MODE = stringPreferencesKey("floating_window_question_mode")
val SAVE_DIR_KEY = stringPreferencesKey("save_dir")
fun apiKeyKeyForModel(model: AIModel): Preferences.Key<String> =
    stringPreferencesKey("api_key_${model.name.lowercase()}")

fun defaultSubModelKeyForModel(model: AIModel): Preferences.Key<String> =
    stringPreferencesKey("default_sub_model_${model.name.lowercase()}")

//Grok

val GROK_SYSTEM_MESSAGE_KEY = stringPreferencesKey("grok_system_message")
val GROK_TEMPERATURE_KEY = doublePreferencesKey("grok_temperature")

//Gemini

val GEMINI_TEMPERATURE_KEY = doublePreferencesKey("gemini_temperature")
val GEMINI_TOP_P_KEY = doublePreferencesKey("gemini_top_p")
val GEMINI_TOP_K_KEY = intPreferencesKey("gemini_top_k")
val GEMINI_RESPONSE_MIME_TYPE_KEY = stringPreferencesKey("gemini_response_mime_type")
val GEMINI_SYSTEM_INSTRUCTION_KEY = stringPreferencesKey("gemini_system_instruction")

//Deepseek
val DEEPSEEK_TEMPERATURE_KEY = doublePreferencesKey("deepseek_temperature")
val DEEPSEEK_SYSTEM_INSTRUCTION_KEY = stringPreferencesKey("deepseek_system_instruction")
