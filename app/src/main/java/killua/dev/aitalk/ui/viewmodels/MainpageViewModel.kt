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
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import killua.dev.aitalk.utils.ClipboardHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
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
    val showGreetings: Boolean = true,
    val searchQuery: String = "",
    val aiResponses: Map<AIModel, AIResponseState> = emptyMap(),
    val subModelMap: Map<AIModel, SubModel> = emptyMap(),
    val searchStartTime: Long? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val showResults: Boolean = false
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
){
    override suspend fun onEvent(state: MainpageUIState, intent: MainpageUIIntent) {
        when(intent){
            is MainpageUIIntent.OnSendButtonClick -> {
                val newQuery = intent.query
                val subModelMap: Map<AIModel, SubModel> = AIModel.entries.associateWith { model ->
                    state.subModelMap[model]
                        ?: apiConfigRepository.getDefaultSubModelForModel(model).firstOrNull()
                        ?: SubModel.entries.first { it.parent == model }
                }
                val apiKeyMap: Map<AIModel, String> = AIModel.entries.associateWith { model ->
                    apiConfigRepository.getApiKeyForModel(model).firstOrNull().orEmpty()
                }
                val filteredSubModelMap: Map<AIModel, SubModel> = subModelMap.filter { (model, _) ->
                    apiKeyMap[model]?.isNotBlank() == true
                }
                val initialResponses: Map<AIModel, AIResponseState> = filteredSubModelMap.keys.associateWith {
                    AIResponseState(status = ResponseStatus.Loading)
                }
                emitState(
                    state.copy(
                        showGreetings = false,
                        searchStartTime = System.currentTimeMillis(),
                        searchQuery = newQuery,
                        aiResponses = initialResponses,
                        subModelMap = filteredSubModelMap
                    )
                )
                aiRepository.fetchAiResponses(newQuery, filteredSubModelMap)
                    .onEach { (model, response) ->
                        updateState { old ->
                            val updatedMap = old.aiResponses.toMutableMap()
                            updatedMap[model] = response
                            old.copy(aiResponses = updatedMap)
                        }
                        if (response.status == ResponseStatus.Error && !response.errorMessage.isNullOrBlank()) {
                            emitEffect(ShowSnackbar("${model.name} 错误: ${response.errorMessage}"))
                        }
                    }
                    .onCompletion {
                        val latestResponses = uiState.value.aiResponses.filter { it.value.status == ResponseStatus.Success }
                        historyRepository.insertHistoryRecord(
                            prompt = newQuery,
                            modelResponses = latestResponses
                        )
                        updateState { old ->
                            old.copy(showResults = true)
                        }
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
            is MainpageUIIntent.ShareResponse -> TODO()
        }
    }
}