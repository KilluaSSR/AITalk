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
    data class DeleteHistory(val id: Long) : HistoryPageUIIntent

    data object DeleteAllHistory: HistoryPageUIIntent
}

data class HistoryPageUIState(
    val historyList: List<SearchHistoryEntity> = emptyList(),
    val isLoading: Boolean = false
) : UIState

@HiltViewModel
class HistoryPageViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
): BaseViewModel<HistoryPageUIIntent, HistoryPageUIState, SnackbarUIEffect>(
    HistoryPageUIState(isLoading = true)
) {
    init {
        viewModelScope.launch {
            historyRepository.getAllHistory()
                .stateInScope(emptyList())
                .collect { list ->
                    emitState(uiState.value.copy(historyList = list, isLoading = false))
                }
        }
    }

    override suspend fun onEvent(state: HistoryPageUIState, intent: HistoryPageUIIntent) {
        when (intent) {
            is HistoryPageUIIntent.DeleteHistory -> {
                historyRepository.deleteRecord(intent.id)
            }

            HistoryPageUIIntent.DeleteAllHistory -> {
                emitState(uiState.value.copy(isLoading = true))
                deleteAllHistory()
                emitState(uiState.value.copy(isLoading = false))
            }
        }
    }

    private suspend fun deleteAllHistory(){
        historyRepository.deleteAll()
    }
}