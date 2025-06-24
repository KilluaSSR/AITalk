package killua.dev.aitalk

import android.content.Context
import androidx.annotation.UiContext
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.datastore.readTheme
import killua.dev.aitalk.datastore.writeSecureMyHistory
import killua.dev.aitalk.datastore.writeTheme
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.utils.BiometricManagerSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsState @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val settingsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        settingsScope.launch {
            loadAllSettings()
        }
    }

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM.name)
    val themeMode: StateFlow<String> = _themeMode

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable: StateFlow<Boolean> = _isBiometricAvailable

    private val _securedHistoryList = MutableStateFlow(false)
    val securedHistoryList: StateFlow<Boolean> = _securedHistoryList

    private suspend fun loadAllSettings(){
        coroutineScope {
            launch { context.readSecureHistory().collect { _securedHistoryList.value = it } }
            launch { context.readTheme().collect { _themeMode.value = it } }
            launch { _isBiometricAvailable.value = BiometricManagerSingleton.getBiometricHelper()?.canAuthenticate() == true }
        }
    }

    suspend fun updateTheme(theme: String){
        context.writeTheme(theme)
        _themeMode.value = theme
    }

    suspend fun updateSecureHistory(set: Boolean){
        context.writeSecureMyHistory(set)
        _securedHistoryList.value = set
    }

    fun onCleared(){
        settingsScope.cancel()
    }
}