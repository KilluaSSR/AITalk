package killua.dev.aitalk.repository

import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.datastore.AppPreferences
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

    // 聚合首屏需要的常用偏好，供 UI 直接订阅，减少多Flow磁盘触发
    val appPreferences: kotlinx.coroutines.flow.StateFlow<AppPreferences>
}