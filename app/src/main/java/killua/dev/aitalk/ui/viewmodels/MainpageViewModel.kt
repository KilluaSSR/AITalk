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
    data object CancelSearch : MainpageUIIntent
    data object RetrySearch : MainpageUIIntent
    data class RetrySpecificModel(val model: AIModel) : MainpageUIIntent
    data class CopyResponse(val model: AIModel) : MainpageUIIntent
    data class ShareResponse(val model: AIModel) : MainpageUIIntent
    data object ClearResults : MainpageUIIntent
    data object LoadSearchHistory : MainpageUIIntent
    data class SelectFromHistory(val query: String) : MainpageUIIntent
    data object OnSendButtonClick : MainpageUIIntent
    data object OnStopButtonClick : MainpageUIIntent
}

data class MainpageUIState(
    val searchQuery: String = "",
    val isInputEnabled: Boolean = true,
    val aiResponses: Map<AIModel, AIResponseState> = emptyMap(),
    val isSearching: Boolean = false,
    val searchStartTime: Long? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val showResults: Boolean = false,
    val searchHistory: List<String> = emptyList(),
) : UIState

class MainpageViewModel @Inject constructor(

): BaseViewModel<MainpageUIIntent, MainpageUIState, SnackbarUIEffect>(
    MainpageUIState()
){

}