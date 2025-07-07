package killua.dev.aitalk.ui.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.FileRepository
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.MainpageState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.ClipboardHelper
import killua.dev.aitalk.utils.prepareAiSearchData
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainpageUIIntent : UIIntent {
    data class UpdateSearchQuery(val query: String) : MainpageUIIntent
    data object ClearInput : MainpageUIIntent
    data object RegenerateAll : MainpageUIIntent
    data class RegenerateSpecificModel(val model: AIModel) : MainpageUIIntent
    data class CopyResponse(val model: AIModel) : MainpageUIIntent
    data class ShareResponse(val model: AIModel) : MainpageUIIntent
    data class SaveSpecificModel(val model: AIModel) : MainpageUIIntent
    data object SaveAll: MainpageUIIntent
    data class OnSendButtonClick(val query: String) : MainpageUIIntent
    data object OnStopButtonClick : MainpageUIIntent
}

data class MainpageUIState(
    val screenState: MainpageState = MainpageState.GREETINGS,
    val searchQuery: String = "",
    val aiResponses: Map<AIModel, AIResponseState> = emptyMap(),
    val subModelMap: Map<AIModel, SubModel> = emptyMap(),
    val searchStartTime: Long? = null,
) : UIState{
    val isSearching: Boolean
        get() = aiResponses.values.any { it.status == ResponseStatus.Loading }
}

@HiltViewModel
class MainpageViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val apiConfigRepository: ApiConfigRepository,
    private val clipboardHelper: ClipboardHelper,
    private val fileRepository: FileRepository,
): BaseViewModel<MainpageUIIntent, MainpageUIState, SnackbarUIEffect>(
    MainpageUIState()
) {

    private var searchJob: Job? = null

    override suspend fun onEvent(state: MainpageUIState, intent: MainpageUIIntent) {
        when(intent){
            is MainpageUIIntent.OnSendButtonClick -> {
                if (intent.query.isBlank()) return

                val hasAnyKey = apiConfigRepository.hasAnyApiKeyBeenSet().first()
                if (!hasAnyKey) {
                    updateState { it.copy(screenState = MainpageState.NO_API_KEY) }
                    return
                }

                val isConfiguredModelEnabled = apiConfigRepository.isAnyModelWithKeyEnabled().first()
                if (!isConfiguredModelEnabled) {
                    updateState { it.copy(screenState = MainpageState.NO_MODELS_ENABLED) }
                    return
                }

                startSearch(intent.query)
            }

            MainpageUIIntent.OnStopButtonClick -> {
                searchJob?.cancel()
                updateState { oldState ->
                    val updatedResponses = oldState.aiResponses.mapValues { (_, response) ->
                        if (response.status == ResponseStatus.Loading) {
                            response.copy(
                                status = ResponseStatus.Error,
                                errorMessage = "Cancelled by user"
                            )
                        } else {
                            response
                        }
                    }
                    oldState.copy(aiResponses = updatedResponses)
                }
            }
            is MainpageUIIntent.UpdateSearchQuery -> {
                emitState(state.copy(searchQuery = intent.query))
            }
            MainpageUIIntent.ClearInput -> {
                emitState(state.copy(searchQuery = ""))
            }
            is MainpageUIIntent.CopyResponse -> {
                val response = state.aiResponses[intent.model]?.content.orEmpty()
                if (response.isNotEmpty()) {
                    clipboardHelper.copy(response)
                    emitEffect(ShowSnackbar("Response copied!"))
                }
            }
            MainpageUIIntent.RegenerateAll -> {
                if (state.searchQuery.isNotBlank()) {
                    startSearch(state.searchQuery)
                }
            }
            is MainpageUIIntent.RegenerateSpecificModel -> {
                val query = state.searchQuery
                val subModel = state.subModelMap[intent.model]
                if (query.isNotBlank() && subModel != null) {
                    viewModelScope.launch {
                        updateState { old ->
                            val updatedMap = old.aiResponses.toMutableMap()
                            updatedMap[intent.model] = AIResponseState(status = ResponseStatus.Loading)
                            old.copy(aiResponses = updatedMap)
                        }
                        aiRepository.fetchAiResponses(query, mapOf(intent.model to subModel))
                            .onEach { (model, response) ->
                                updateState { old ->
                                    val updatedMap = old.aiResponses.toMutableMap()
                                    updatedMap[model] = response
                                    old.copy(aiResponses = updatedMap)
                                }
                            }
                            .catch { e ->
                                updateState { old ->
                                    val updatedMap = old.aiResponses.toMutableMap()
                                    updatedMap[intent.model] = AIResponseState(
                                        status = ResponseStatus.Error,
                                        errorMessage = e.message ?: "An unknown error occurred"
                                    )
                                    old.copy(aiResponses = updatedMap)
                                }
                                emitEffect(ShowSnackbar("${intent.model.name} regeneration failed: ${e.message}"))
                            }
                            .launchIn(this)
                    }
                }
            }
            MainpageUIIntent.SaveAll -> {
                val allSuccess = state.aiResponses.values.all { it.status == ResponseStatus.Success && !it.content.isNullOrBlank() }
                if (allSuccess) {
                    val saveDir = settingsRepository.getSaveDir().firstOrNull().orEmpty()
                    val directoryUri = if (saveDir.isNotBlank() && saveDir != DEFAULT_SAVE_DIR) saveDir.toUri() else null
                    fileRepository.saveAllResponsesToFile(
                        prompt = state.searchQuery,
                        responses = state.aiResponses,
                        directoryUri = directoryUri
                    )
                    emitEffect(ShowSnackbar("全部保存成功"))
                } else {
                    emitEffect(ShowSnackbar("请等待所有模型回复完成后再保存"))
                }
            }
            is MainpageUIIntent.SaveSpecificModel -> {
                val responseState = state.aiResponses[intent.model]
                if (responseState?.status == ResponseStatus.Success && !responseState.content.isNullOrBlank()) {
                    val saveDir = settingsRepository.getSaveDir().firstOrNull().orEmpty()
                    val directoryUri = if (saveDir.isNotBlank() && saveDir != DEFAULT_SAVE_DIR) saveDir.toUri() else null
                    fileRepository.saveResponseToFile(
                        model = intent.model,
                        prompt = state.searchQuery,
                        response = responseState.content,
                        directoryUri = directoryUri
                    )
                    emitEffect(ShowSnackbar("保存成功"))
                } else {
                    emitEffect(ShowSnackbar("该模型回复未完成，无法保存"))
                }
            }
            is MainpageUIIntent.ShareResponse -> {
                // Implement share logic here if needed
            }
        }
    }

    private fun startSearch(query: String) {
        searchJob?.cancel()
        viewModelScope.launch {
            val (filteredSubModelMap, _, initialResponses) = prepareAiSearchData(
                apiConfigRepository = apiConfigRepository,
                currentSubModelMap = uiState.value.subModelMap
            )
            emitState(
                uiState.value.copy(
                    screenState = MainpageState.SHOWING_RESULTS,
                    searchStartTime = System.currentTimeMillis(),
                    searchQuery = query,
                    aiResponses = initialResponses,
                    subModelMap = filteredSubModelMap
                )
            )
            searchJob = aiRepository.fetchAiResponses(query, filteredSubModelMap)
                .onEach { (model, response) ->
                    updateState { old ->
                        val updatedMap = old.aiResponses.toMutableMap()
                        updatedMap[model] = response
                        old.copy(aiResponses = updatedMap)
                    }
                    if (response.status == ResponseStatus.Error && !response.errorMessage.isNullOrBlank()) {
                        emitEffect(ShowSnackbar("${model.name} Error: ${response.errorMessage}"))
                    }
                }
                .onCompletion { throwable ->
                    if (throwable == null) {
                        val latestResponses = uiState.value.aiResponses.filter { it.value.status == ResponseStatus.Success }
                        historyRepository.insertHistoryRecord(
                            prompt = query,
                            modelResponses = latestResponses
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}