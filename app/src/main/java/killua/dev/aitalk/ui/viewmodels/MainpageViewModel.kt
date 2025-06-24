package killua.dev.aitalk.ui.viewmodels

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import javax.inject.Inject

sealed interface MainpageUIIntent : UIIntent {
    data class UpdateSearchQuery(val query: String) : MainpageUIIntent
    data class StartSearch(val query: String) : MainpageUIIntent
    data object ClearInput : MainpageUIIntent
    data object RevokeAll : MainpageUIIntent
    data object RegenerateAll : MainpageUIIntent
    data class RegenerateSpecificModel(val model: AIModel) : MainpageUIIntent
    data class CopyResponse(val model: AIModel) : MainpageUIIntent
    data class ShareResponse(val model: AIModel) : MainpageUIIntent
    data class SaveSpecificModel(val model: AIModel) : MainpageUIIntent
    data object SaveAll: MainpageUIIntent
    data object OnSendButtonClick : MainpageUIIntent
    data object OnStopButtonClick : MainpageUIIntent
}

data class MainpageUIState(
    val showGreetings: Boolean = true,
    val searchQuery: String = "",
    val aiResponses: Map<AIModel, AIResponseState> = emptyMap(),
    val isSearching: Boolean = false,
    val searchStartTime: Long? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val showResults: Boolean = false,
    val searchHistory: List<String> = emptyList(),
) : UIState

class MainpageViewModel @Inject constructor(): BaseViewModel<MainpageUIIntent, MainpageUIState, SnackbarUIEffect>(
    MainpageUIState()
){
    override suspend fun onEvent(state: MainpageUIState, intent: MainpageUIIntent) {
        when(intent){
            MainpageUIIntent.OnSendButtonClick -> {
                emitState(uiState.value.copy(showGreetings = false, isSearching = true))
            }

            MainpageUIIntent.OnStopButtonClick -> {

            }
            is MainpageUIIntent.UpdateSearchQuery -> {

            }
            MainpageUIIntent.ClearInput -> TODO()
            is MainpageUIIntent.CopyResponse -> TODO()
            MainpageUIIntent.RegenerateAll -> TODO()
            is MainpageUIIntent.RegenerateSpecificModel -> TODO()
            MainpageUIIntent.RevokeAll -> TODO()
            MainpageUIIntent.SaveAll -> TODO()
            is MainpageUIIntent.SaveSpecificModel -> TODO()
            is MainpageUIIntent.ShareResponse -> TODO()
            is MainpageUIIntent.StartSearch -> TODO()
        }
    }
}