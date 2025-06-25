package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryPageUIIntent : UIIntent {
    object LoadHistory : HistoryPageUIIntent
    data class DeleteHistory(val id: Long) : HistoryPageUIIntent
}

data class HistoryPageUIState(
    val historyList: List<SearchHistoryEntity> = emptyList(),
    val isLoading: Boolean = false
) : UIState

@HiltViewModel
class HistoryPageViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
): BaseViewModel<HistoryPageUIIntent, HistoryPageUIState, SnackbarUIEffect>(
    HistoryPageUIState()
) {
    init {
        viewModelScope.launch {
            historyRepository.getAllHistory()
                .stateInScope(emptyList())
                .collect { list ->
                    emitState(uiState.value.copy(historyList = list))
                }
        }
    }

    override suspend fun onEvent(state: HistoryPageUIState, intent: HistoryPageUIIntent) {
        when (intent) {
            HistoryPageUIIntent.LoadHistory -> {
                emitState(state.copy(isLoading = true))
                // Load history from repository
                emitState(state.copy(isLoading = false))
            }
            is HistoryPageUIIntent.DeleteHistory -> {
                historyRepository.deleteRecord(intent.id)
            }
        }
    }
}