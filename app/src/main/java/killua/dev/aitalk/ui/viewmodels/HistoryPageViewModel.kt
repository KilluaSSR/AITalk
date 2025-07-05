package killua.dev.aitalk.ui.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.repository.FileRepository
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.ClipboardHelper
import killua.dev.aitalk.utils.toSavable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

sealed interface HistoryPageUIIntent : UIIntent {
    data class DeleteHistory(val id: Long) : HistoryPageUIIntent

    data object DeleteAllHistory: HistoryPageUIIntent
    data class CopyPrompt(val prompt: String) : HistoryPageUIIntent
    data class SavePrompt(val index: Long) : HistoryPageUIIntent
}

data class HistoryPageUIState(
    val isLoading: Boolean = false
) : UIState

@HiltViewModel
class HistoryPageViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val clipboardHelper: ClipboardHelper,
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository,
): BaseViewModel<HistoryPageUIIntent, HistoryPageUIState, SnackbarUIEffect>(
    HistoryPageUIState(isLoading = true)
) {
    val pagedHistory: Flow<PagingData<SearchHistoryEntity>> =
        historyRepository.getPagedHistory().cachedIn(viewModelScope)

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
            is HistoryPageUIIntent.CopyPrompt -> {
                if (intent.prompt.isNotEmpty()) {
                    clipboardHelper.copy(intent.prompt)
                }
            }

            is HistoryPageUIIntent.SavePrompt -> {
                val entity = historyRepository.getSpecificRecord(intent.index)
                if(entity != null){
                    val saveDir = settingsRepository.getSaveDir().firstOrNull().orEmpty()
                    val directoryUri = if (saveDir.isNotBlank() && saveDir != DEFAULT_SAVE_DIR) saveDir.toUri() else null
                    fileRepository.saveAllResponsesToFile(
                        prompt = entity.prompt,
                        responses = entity.toSavable(),
                        directoryUri = directoryUri
                    )
                }else{
                    emitEffect(SnackbarUIEffect.ShowSnackbar("Internal Error"))
                }
            }
        }
    }

    private suspend fun deleteAllHistory(){
        historyRepository.deleteAll()
    }
}