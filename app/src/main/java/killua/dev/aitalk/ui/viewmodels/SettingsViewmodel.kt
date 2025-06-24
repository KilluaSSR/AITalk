package killua.dev.aitalk.ui.viewmodels

import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.BiometricManagerSingleton

sealed interface SettingsUIIntent: UIIntent{
    data object onArrive: SettingsUIIntent
}

data class SettingsUIState(
    val isBiometricAvailable: Boolean = false
): UIState
class SettingsViewmodel : BaseViewModel<SettingsUIIntent, SettingsUIState , SnackbarUIEffect>(
    SettingsUIState()
){
    override suspend fun onEvent(state: SettingsUIState, intent: SettingsUIIntent) {
        when(intent){
            SettingsUIIntent.onArrive -> {
                emitState(uiState.value.copy(
                    isBiometricAvailable = BiometricManagerSingleton.getBiometricHelper()?.canAuthenticate() == true
                ))
            }
        }
    }
}