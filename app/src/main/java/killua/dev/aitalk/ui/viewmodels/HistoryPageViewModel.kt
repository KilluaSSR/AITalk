package killua.dev.aitalk.ui.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.R
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.repository.FileRepository
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.ClipboardHelper
import killua.dev.aitalk.utils.toSavableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryPageUIIntent : UIIntent {
    data class DeleteHistory(val id: Long) : HistoryPageUIIntent
    data object ShowDeleteAllDialog: HistoryPageUIIntent
    data object DeleteAllHistory: HistoryPageUIIntent
    data class CopyPrompt(val prompt: String) : HistoryPageUIIntent
    data class SavePrompt(val index: Long) : HistoryPageUIIntent
    data class CopyResponse(val responseContent: String) : HistoryPageUIIntent
    object SubmitSearch : HistoryPageUIIntent
    data class SearchQueryChanged(val query: String) : HistoryPageUIIntent
}

data class HistoryPageUIState(
    val isLoading: Boolean = false,
    val isDeleteAllDialogVisible: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
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
    private val _deletingItemIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingItemIds = _deletingItemIds.asStateFlow()

    // 轻量建议列表（限制前10条，避免一次性加载全部）
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchSuggestions: StateFlow<List<SearchHistoryEntity>> = uiState
        .map { it.searchQuery }
        .debounce(200L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) flowOf(emptyList()) else historyRepository.searchHistory(query)
        }
        .map { it.take(10) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 分页搜索结果（激活搜索模式时使用）
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedSearchResults: Flow<PagingData<SearchHistoryEntity>> = uiState
        .map { it.searchQuery }
        .debounce(250L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(PagingData.empty())
            } else {
                historyRepository.searchPagedHistory(query)
            }
        }
        .cachedIn(viewModelScope)
    override suspend fun onEvent(state: HistoryPageUIState, intent: HistoryPageUIIntent) {
        when (intent) {
            is HistoryPageUIIntent.DeleteHistory -> {
                _deletingItemIds.value = _deletingItemIds.value + intent.id
                viewModelScope.launch {
                    delay(500)
                    historyRepository.deleteRecord(intent.id)
                    _deletingItemIds.value = _deletingItemIds.value - intent.id
                }
            }
            is HistoryPageUIIntent.SearchQueryChanged -> {
                val trimmed = intent.query.trim()
                val searchActive = if (trimmed.isBlank()) false else state.isSearchActive
                emitState(state.copy(searchQuery = trimmed, isSearchActive = searchActive))
            }
            HistoryPageUIIntent.DeleteAllHistory -> {
                emitState(uiState.value.copy(isLoading = true))
                deleteAllHistory()
                emitState(uiState.value.copy(isLoading = false))
            }
            is HistoryPageUIIntent.CopyPrompt -> {
                if (intent.prompt.isNotEmpty()) {
                    clipboardHelper.copy(intent.prompt)
                    emitEffect(ShowSnackbar("已复制到剪贴板"))
                }
            }

            is HistoryPageUIIntent.SavePrompt -> {
                val entity = historyRepository.getSpecificRecord(intent.index)
                if(entity != null){
                    val saveDir = settingsRepository.getSaveDir().firstOrNull().orEmpty()
                    val directoryUri = if (saveDir.isNotBlank() && saveDir != DEFAULT_SAVE_DIR) saveDir.toUri() else null
                    fileRepository.saveAllResponsesToFile(
                        prompt = entity.prompt,
                        responses = entity.toSavableMap(),
                        directoryUri = directoryUri
                    ).onSuccess { emitEffect(ShowSnackbar("已保存全部响应")) }
                }else{
                    emitEffect(ShowSnackbar("内部错误"))
                }
            }

            is HistoryPageUIIntent.CopyResponse -> {
                if (intent.responseContent.isNotEmpty()) {
                    clipboardHelper.copy(intent.responseContent)
                    emitEffect(ShowSnackbar("已复制到剪贴板"))
                }
            }

            HistoryPageUIIntent.ShowDeleteAllDialog -> {
                emitState(uiState.value.copy(isDeleteAllDialogVisible = true))
            }
            HistoryPageUIIntent.SubmitSearch -> {
                if (state.searchQuery.length >= 2) {
                    emitState(state.copy(isSearchActive = true))
                }
            }
        }
    }

    private suspend fun deleteAllHistory(){
        historyRepository.deleteAll()
    }
}