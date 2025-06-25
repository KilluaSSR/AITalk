package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    data object GoToOverlaySettingsClicked : SettingsUIIntent
    data object OnArrive : SettingsUIIntent
}

sealed interface SettingsNavigationEvent {
    data object NavigateToOverlaySettings : SettingsNavigationEvent
}

data class SettingsUIState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isBiometricAvailable: Boolean = false,
    val isHistorySecured: Boolean = false,
    val canDrawOverlays: Boolean = false
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

        viewModelScope.launch {
            combine(themeFlow, securedHistoryFlow) { themeName, isSecured ->
                SettingsUIState(
                    isLoading = false,
                    themeMode = ThemeMode.valueOf(themeName),
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
                // 其他检查
                emitState(state.copy(canDrawOverlays = canOverlay, isLoading = false))
            }
        }
    }
}