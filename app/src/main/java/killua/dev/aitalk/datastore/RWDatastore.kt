package killua.dev.aitalk.datastore

import android.content.Context
import android.util.Log
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.consts.SYSTEM_LOCALE_TAG
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.utils.getFloatingWindowInstructionStringRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun Context.readTheme() = readStoreString(THEME_MODE, defValue = ThemeMode.SYSTEM.name)
fun Context.readLocale() = readStoreString(LOCALE_MODE, defValue = SYSTEM_LOCALE_TAG)
fun Context.readSecureHistory() = readStoreBoolean(key = SECURE_HISTORY, defValue = false)
fun Context.readFloatingWindowQuestionMode(): Flow<FloatingWindowQuestionMode> =
    readStoreString(
        FLOATING_WINDOW_QUESTION_MODE,
        defValue = FloatingWindowQuestionMode.isThatTrueWithExplain.name
    )
        .map { name ->
            FloatingWindowQuestionMode.entries.firstOrNull { it.name == name }
                ?: FloatingWindowQuestionMode.isThatTrueWithExplain
        }

fun Context.readSaveDir(defValue: String = DEFAULT_SAVE_DIR) =
    readStoreString(SAVE_DIR_KEY, defValue)

fun Context.readFloatingWindowSystemInstruction(
    questionMode: FloatingWindowQuestionMode,
    model: AIModel
): Flow<String> =
    dataStore.data.map { preferences ->
        val instructionKey = floatingWindowSystemInstructionKey(questionMode, model)
        val userCustomInstruction = preferences[instructionKey]
        userCustomInstruction ?: getString(
            getFloatingWindowInstructionStringRes(
                questionMode,
                model
            )
        )
    }

fun Context.readApiKeyForModel(model: AIModel, defValue: String = "") =
    readStoreString(apiKeyKeyForModel(model), defValue)

fun Context.readDefaultSubModelForModel(model: AIModel, defValue: String = "") =
    readStoreString(defaultSubModelKeyForModel(model), defValue)


suspend fun Context.writeFloatingWindowSystemInstruction(
    questionMode: FloatingWindowQuestionMode,
    model: AIModel,
    instruction: String
) =
    saveStoreString(floatingWindowSystemInstructionKey(questionMode, model), instruction)


suspend fun Context.writeDefaultSubModelForModel(model: AIModel, subModel: SubModel) {
    Log.d(
        "DataStore",
        "Setting default sub model for model ${defaultSubModelKeyForModel(model)}. Value: ${subModel.name}"
    )
    saveStoreString(defaultSubModelKeyForModel(model), subModel.name)
}

suspend fun Context.writeLocale(locale: String) = saveStoreString(LOCALE_MODE, locale)
suspend fun Context.writeTheme(theme: String) = saveStoreString(THEME_MODE, theme)
suspend fun Context.writeSecureMyHistory(set: Boolean) = saveStoreBoolean(SECURE_HISTORY, set)

suspend fun Context.writeFloatingWindowQuestionMode(mode: FloatingWindowQuestionMode) =
    saveStoreString(FLOATING_WINDOW_QUESTION_MODE, mode.name)

suspend fun Context.writeApiKeyForModel(model: AIModel, apiKey: String) =
    saveStoreString(apiKeyKeyForModel(model), apiKey)

suspend fun Context.writeSaveDir(dir: String) =
    saveStoreString(SAVE_DIR_KEY, dir)


//Grok
fun Context.readGrokSystemMessage(defValue: String = "You are Grok, a chatbot inspired by the Hitchhikers Guide to the Galaxy.") =
    readStoreString(GROK_SYSTEM_MESSAGE_KEY, defValue)

fun Context.readGrokTemperature(defValue: Double = 0.0) =
    readStoreDouble(GROK_TEMPERATURE_KEY, defValue)

suspend fun Context.writeGrokSystemMessage(message: String) =
    saveStoreString(GROK_SYSTEM_MESSAGE_KEY, message)

suspend fun Context.writeGrokTemperature(temperature: Double) =
    saveStoreDouble(GROK_TEMPERATURE_KEY, temperature)

//Gemini
fun Context.readGeminiTemperature(defValue: Double = 1.0) =
    readStoreDouble(GEMINI_TEMPERATURE_KEY, defValue)

fun Context.readGeminiTopP(defValue: Double = 0.95) =
    readStoreDouble(GEMINI_TOP_P_KEY, defValue)

fun Context.readGeminiTopK(defValue: Int = 40) =
    readStoreInt(GEMINI_TOP_K_KEY, defValue)

fun Context.readGeminiResponseMimeType(defValue: String = "text/plain") =
    readStoreString(GEMINI_RESPONSE_MIME_TYPE_KEY, defValue)

fun Context.readGeminiSystemInstruction(defValue: String = "You are a helpful assistant.") =
    // 默认值，如果 curl 示例中是 "You are a cat. Your name is Neko."，则改为那个
    readStoreString(GEMINI_SYSTEM_INSTRUCTION_KEY, defValue)

suspend fun Context.writeGeminiTemperature(temperature: Double) =
    saveStoreDouble(GEMINI_TEMPERATURE_KEY, temperature)

suspend fun Context.writeGeminiTopP(topP: Double) =
    saveStoreDouble(GEMINI_TOP_P_KEY, topP)

suspend fun Context.writeGeminiTopK(topK: Int) =
    saveStoreInt(GEMINI_TOP_K_KEY, topK)

suspend fun Context.writeGeminiResponseMimeType(mimeType: String) =
    saveStoreString(GEMINI_RESPONSE_MIME_TYPE_KEY, mimeType)

suspend fun Context.writeGeminiSystemInstruction(instruction: String) =
    saveStoreString(GEMINI_SYSTEM_INSTRUCTION_KEY, instruction)

//Deekseek
fun Context.readDeepSeekTemperature(defValue: Double = 1.0) =
    readStoreDouble(DEEPSEEK_TEMPERATURE_KEY, defValue)

fun Context.readDeepSeekSystemInstruction(defValue: String = "You are a helpful assistant.") =
    readStoreString(DEEPSEEK_SYSTEM_INSTRUCTION_KEY, defValue)

suspend fun Context.writeDeepSeekTemperature(temperature: Double) =
    saveStoreDouble(DEEPSEEK_TEMPERATURE_KEY, temperature)

suspend fun Context.writeDeepSeekSystemInstruction(instruction: String) =
    saveStoreString(DEEPSEEK_SYSTEM_INSTRUCTION_KEY, instruction)