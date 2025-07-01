package killua.dev.aitalk.ui.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.consts.DEFAULT_SAVE_DIR
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.AiRepository
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

sealed interface FloatingWindowUIIntent: UIIntent{
    data class StartSearch(val query: String) : FloatingWindowUIIntent
    data class CopyResponse(val model: AIModel) : FloatingWindowUIIntent
    data class RegenerateSpecificModel(val model: AIModel) : FloatingWindowUIIntent
    data class SaveSpecificModel(val model: AIModel) : FloatingWindowUIIntent
}

data class FloatingWindowUIState(
    val searchQuery: String = "",
    val aiResponses: Map<AIModel, AIResponseState> = emptyMap(),
    val isSearching: Boolean = true,
    val searchStartTime: Long? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val showResults: Boolean = false,
): UIState

@HiltViewModel
class FloatingWindowViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val clipboardHelper: ClipboardHelper,
    private val fileRepository: FileRepository,
): BaseViewModel<FloatingWindowUIIntent, FloatingWindowUIState, SnackbarUIEffect>(
    FloatingWindowUIState()
) {
    override suspend fun onEvent(state: FloatingWindowUIState, intent: FloatingWindowUIIntent) {
        when(intent){
            is FloatingWindowUIIntent.StartSearch -> {
                val initialResponses = AIModel.entries.associateWith {
                    AIResponseState(status = ResponseStatus.Loading)
                }
                emitState(uiState.value.copy(
                    isSearching = true,
                    searchStartTime = System.currentTimeMillis(),
                    searchQuery = intent.query,
                    aiResponses = initialResponses
                ))
                aiRepository.fetchAiResponses(intent.query)
                    .onEach { (model, response) ->
                        updateState { old ->
                            val updatedMap = old.aiResponses.toMutableMap()
                            updatedMap[model] = response
                            old.copy(aiResponses = updatedMap)
                        }
                    }
                    .onCompletion {
                        val latestResponses = uiState.value.aiResponses
                        historyRepository.insertHistoryRecord(
                            prompt = intent.query,
                            modelResponses = latestResponses
                        )
                        updateState { old ->
                            old.copy(isSearching = false, showResults = true)
                        }
                    }
                    .launchIn(viewModelScope)
            }
            is FloatingWindowUIIntent.CopyResponse ->{
                val response = state.aiResponses[intent.model]?.content.orEmpty()
                if (response.isNotEmpty()) {
                    clipboardHelper.copy(response)
                }
            }

            is FloatingWindowUIIntent.RegenerateSpecificModel -> {

            }

            is FloatingWindowUIIntent.SaveSpecificModel -> {
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
        }
    }
}