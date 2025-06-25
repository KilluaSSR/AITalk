package killua.dev.aitalk.repository

import android.content.Context

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getThemeMode(): Flow<String>
    fun isHistorySecured(): Flow<Boolean>

    fun isBiometricAvailable(): Boolean
    fun canDrawOverlays(): Boolean

    suspend fun setThemeMode(theme: String)
    suspend fun setHistorySecured(isSecured: Boolean)
}