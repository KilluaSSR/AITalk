package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUIIntent : UIIntent {
    data class UpdateTheme(val theme: ThemeMode) : SettingsUIIntent
    data class UpdateSecureHistory(val isEnabled: Boolean) : SettingsUIIntent
    data class UpdateQuestionMode(val mode: FloatingWindowQuestionMode) : SettingsUIIntent
    data object GoToOverlaySettingsClicked : SettingsUIIntent
    data object OnArrive : SettingsUIIntent
    data object ChooseSaveDir : SettingsUIIntent
    data class SaveDirSelected(val uri: String) : SettingsUIIntent
}

sealed interface SettingsNavigationEvent {
    data object NavigateToOverlaySettings : SettingsNavigationEvent

}

data class SettingsUIState(
    val isLoading: Boolean = true,
    val saveDir: String = "",
    val isChoosingDir: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isBiometricAvailable: Boolean = false,
    val isHistorySecured: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val questionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain
) : UIState

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    private val repository: SettingsRepository
) : BaseViewModel<SettingsUIIntent, SettingsUIState , SnackbarUIEffect>(
    SettingsUIState()
){
    private val _navigationEvent = MutableSharedFlow<SettingsNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        val themeFlow = repository.getThemeMode()
        val securedHistoryFlow = repository.isHistorySecured()
        val questionMode = repository.getFloatingWindowQuestionMode()
        val saveDir = repository.getSaveDir()
        viewModelScope.launch {
            combine(themeFlow, securedHistoryFlow, questionMode, saveDir) { themeName, isSecured, questionMode, saveDir ->
                SettingsUIState(
                    isLoading = false,
                    themeMode = ThemeMode.valueOf(themeName),
                    saveDir = saveDir,
                    questionMode = questionMode,
                    isBiometricAvailable = repository.isBiometricAvailable(),
                    isHistorySecured = isSecured,
                    canDrawOverlays = repository.canDrawOverlays()
                )
            }.collect { newState ->
                emitState(newState)
            }
        }
    }

    override suspend fun onEvent(state: SettingsUIState, intent: SettingsUIIntent) {
        when (intent) {
            is SettingsUIIntent.UpdateTheme -> {
                repository.setThemeMode(intent.theme.name)
            }
            is SettingsUIIntent.UpdateSecureHistory -> {
                repository.setHistorySecured(intent.isEnabled)
            }
            is SettingsUIIntent.GoToOverlaySettingsClicked -> {
                if (!state.canDrawOverlays) {
                    viewModelScope.launch {
                        _navigationEvent.emit(SettingsNavigationEvent.NavigateToOverlaySettings)
                    }
                }
            }
            is SettingsUIIntent.OnArrive -> {
                emitState(state.copy(isLoading = true))
                val canOverlay = repository.canDrawOverlays()
                emitState(state.copy(canDrawOverlays = canOverlay, isLoading = false))
            }

            is SettingsUIIntent.UpdateQuestionMode -> {
                repository.setFloatingWindowQuestionMode(intent.mode)
                emitState(state.copy(questionMode = intent.mode))
            }
            is SettingsUIIntent.ChooseSaveDir -> {
                emitState(state.copy(isChoosingDir = true))
            }
            is SettingsUIIntent.SaveDirSelected -> {
                repository.setSaveDir(intent.uri)
                emitState(state.copy(saveDir = intent.uri, isChoosingDir = false))
            }
        }
    }
}