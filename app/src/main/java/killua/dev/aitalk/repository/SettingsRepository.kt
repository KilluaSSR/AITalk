package killua.dev.aitalk.repository

import killua.dev.aitalk.models.FloatingWindowQuestionMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getThemeMode(): Flow<String>
    fun isHistorySecured(): Flow<Boolean>
    fun getFloatingWindowQuestionMode(): Flow<FloatingWindowQuestionMode>
    fun isBiometricAvailable(): Boolean
    fun canDrawOverlays(): Boolean
    fun getSaveDir(): Flow<String>
    suspend fun setThemeMode(theme: String)
    suspend fun setFloatingWindowQuestionMode(mode: FloatingWindowQuestionMode)
    suspend fun setHistorySecured(isSecured: Boolean)
    suspend fun setSaveDir(dir: String)

    suspend fun setLocale(locale: String)
    fun readLocale(): Flow<String>

    suspend fun changeMyLanguage(locale: String)
}