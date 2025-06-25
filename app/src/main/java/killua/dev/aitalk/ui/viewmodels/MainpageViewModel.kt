package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.AiRepository
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.ClipboardHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.launchIn

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

@HiltViewModel
class MainpageViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val historyRepository: HistoryRepository,
    private val clipboardHelper: ClipboardHelper
): BaseViewModel<MainpageUIIntent, MainpageUIState, SnackbarUIEffect>(
    MainpageUIState()
){
    override suspend fun onEvent(state: MainpageUIState, intent: MainpageUIIntent) {
        when(intent){
            MainpageUIIntent.OnSendButtonClick -> {
                emitState(uiState.value.copy(
                    showGreetings = false,
                    isSearching = true,
                    searchStartTime = System.currentTimeMillis(),
                    aiResponses = AIModel.entries.associateWith {
                        AIResponseState(status = ResponseStatus.Loading)
                    }
                ))
                aiRepository.fetchAiResponses(state.searchQuery)
                    .onEach { (model, response) ->
                        updateState { old ->
                            val updatedMap = old.aiResponses.toMutableMap()
                            updatedMap[model] = response
                            old.copy(aiResponses = updatedMap)
                        }
                    }
                    .onCompletion {
                        updateState { old ->
                            old.copy(isSearching = false, showResults = true)
                        }
                        historyRepository.insertHistoryRecord(
                            prompt = state.searchQuery,
                            modelResponses = uiState.value.aiResponses
                        )
                    }
                    .launchIn(viewModelScope)
            }

            MainpageUIIntent.OnStopButtonClick -> {

            }
            is MainpageUIIntent.UpdateSearchQuery -> {

            }
            MainpageUIIntent.ClearInput -> TODO()
            is MainpageUIIntent.CopyResponse -> {
                val response = state.aiResponses[intent.model]?.content.orEmpty()
                if (response.isNotEmpty()) {
                    clipboardHelper.copy(response)
                }
            }
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