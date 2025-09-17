package killua.dev.aitalk.repository.impl

import android.content.Context
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.consts.SYSTEM_LOCALE_TAG
import killua.dev.aitalk.datastore.readFloatingWindowQuestionMode
import killua.dev.aitalk.datastore.readLocale
import killua.dev.aitalk.datastore.readSaveDir
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.datastore.readTheme
import killua.dev.aitalk.datastore.writeFloatingWindowQuestionMode
import killua.dev.aitalk.datastore.writeLocale
import killua.dev.aitalk.datastore.writeSaveDir
import killua.dev.aitalk.datastore.writeSecureMyHistory
import killua.dev.aitalk.datastore.writeTheme
import killua.dev.aitalk.datastore.AppPreferences
import killua.dev.aitalk.datastore.dataStore
import killua.dev.aitalk.datastore.SECURE_HISTORY
import killua.dev.aitalk.datastore.THEME_MODE
import killua.dev.aitalk.datastore.LOCALE_MODE
import killua.dev.aitalk.datastore.FLOATING_WINDOW_QUESTION_MODE
import killua.dev.aitalk.datastore.SAVE_DIR_KEY
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.utils.BiometricManagerSingleton
import killua.dev.aitalk.utils.withMainContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override val appPreferences = context.dataStore.data
        .map { prefs ->
            AppPreferences(
                themeMode = ThemeMode.valueOf(prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name),
                secureHistory = prefs[SECURE_HISTORY] ?: false,
                locale = prefs[LOCALE_MODE] ?: SYSTEM_LOCALE_TAG,
                floatingWindowQuestionMode = prefs[FLOATING_WINDOW_QUESTION_MODE]?.let { stored ->
                    runCatching { killua.dev.aitalk.models.FloatingWindowQuestionMode.valueOf(stored) }
                        .getOrDefault(killua.dev.aitalk.models.FloatingWindowQuestionMode.isThatTrueWithExplain)
                } ?: killua.dev.aitalk.models.FloatingWindowQuestionMode.isThatTrueWithExplain,
                saveDir = prefs[SAVE_DIR_KEY] ?: DEFAULT_SAVE_DIR
            )
        }
        .stateIn(appScope, SharingStarted.Eagerly, AppPreferences())

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

    override suspend fun setLocale(locale: String) {
        context.writeLocale(locale)
    }

    override fun readLocale(): Flow<String> = context.readLocale()

    override suspend fun changeMyLanguage(locale: String) {
        withMainContext{
            val localeList = if (locale == SYSTEM_LOCALE_TAG) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(locale)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
