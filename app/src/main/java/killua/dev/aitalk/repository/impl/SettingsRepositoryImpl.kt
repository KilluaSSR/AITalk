package killua.dev.aitalk.repository.impl

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.datastore.readFloatingWindowQuestionMode
import killua.dev.aitalk.datastore.readFloatingWindowSystemInstruction
import killua.dev.aitalk.datastore.readSaveDir
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.datastore.readTheme
import killua.dev.aitalk.datastore.writeFloatingWindowQuestionMode
import killua.dev.aitalk.datastore.writeSaveDir
import killua.dev.aitalk.datastore.writeSecureMyHistory
import killua.dev.aitalk.datastore.writeTheme
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.utils.BiometricManagerSingleton
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    override fun getThemeMode(): Flow<String> = context.readTheme()
    override fun isHistorySecured(): Flow<Boolean> = context.readSecureHistory()

    override fun isBiometricAvailable(): Boolean {
        return BiometricManagerSingleton.getBiometricHelper()?.canAuthenticate() == true
    }
    override fun getFloatingWindowQuestionMode(): Flow<FloatingWindowQuestionMode> {
        return context.readFloatingWindowQuestionMode()
    }

    override fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    override suspend fun setThemeMode(theme: String) {
        context.writeTheme(theme)
    }

    override suspend fun setFloatingWindowQuestionMode(mode: FloatingWindowQuestionMode) {
        context.writeFloatingWindowQuestionMode(mode)
    }

    override suspend fun setHistorySecured(isSecured: Boolean) {
        context.writeSecureMyHistory(isSecured)
    }

    override fun getSaveDir(): Flow<String> =
        context.readSaveDir(defValue = DEFAULT_SAVE_DIR)

    override suspend fun setSaveDir(dir: String) {
        context.writeSaveDir(dir)
    }
}
