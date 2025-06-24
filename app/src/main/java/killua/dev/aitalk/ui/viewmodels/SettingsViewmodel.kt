package killua.dev.aitalk.ui.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.SettingsState
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import javax.inject.Inject

sealed interface SettingsUIIntent: UIIntent{
    data object onArrive: SettingsUIIntent
}

data class SettingsUIState(
    val isBiometricAvailable: Boolean = false
): UIState

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    val settingsState: SettingsState
) : BaseViewModel<SettingsUIIntent, SettingsUIState , SnackbarUIEffect>(
    SettingsUIState()
){
    override fun onCleared() {
        super.onCleared()
        settingsState.onCleared()
    }
}