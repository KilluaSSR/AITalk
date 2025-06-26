package killua.dev.aitalk.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getThemeMode(): Flow<String>
    fun isHistorySecured(): Flow<Boolean>
    fun getFloatingWindowQuestionMode(): Flow<String>
    fun isBiometricAvailable(): Boolean
    fun canDrawOverlays(): Boolean
    fun getSaveDir(): Flow<String>
    suspend fun setThemeMode(theme: String)
    suspend fun setFloatingWindowQuestionMode(mode: String)
    suspend fun setHistorySecured(isSecured: Boolean)
    suspend fun setSaveDir(dir: String)
}