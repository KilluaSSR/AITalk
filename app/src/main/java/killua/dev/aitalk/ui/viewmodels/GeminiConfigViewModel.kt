package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.api.GeminiConfig
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GeminiConfigUIState(
    val subModels: List<SubModel> = emptyList(),
    val optionIndex: Int = 0,
    val apiKey: String = "",
    val isModelEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val currentFloatingWindowQuestionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain,
    val editableFloatingWindowSystemInstruction: String = "",
    val geminiConfig: GeminiConfig = GeminiConfig(),
) : UIState

sealed interface GeminiConfigUIIntent : UIIntent {
    data class UpdateApiKey(val apiKey: String) : GeminiConfigUIIntent
    data object SaveSettings : GeminiConfigUIIntent
    data class ToggleModelEnabled(val isEnabled: Boolean) : GeminiConfigUIIntent
    data class SelectOption(val index: Int) : GeminiConfigUIIntent
    data class UpdateFloatingWindowSystemInstruction(val instruction: String) : GeminiConfigUIIntent
    data class UpdateGeminiTemperature(val temperature: Double) : GeminiConfigUIIntent
    data class UpdateGeminiTopP(val topP: Double) : GeminiConfigUIIntent
    data class UpdateGeminiTopK(val topK: Int) : GeminiConfigUIIntent
    data class UpdateGeminiResponseMimeType(val mimeType: String) : GeminiConfigUIIntent
    data class UpdateGeminiSystemInstruction(val instruction: String) : GeminiConfigUIIntent
}

@HiltViewModel
class GeminiConfigViewModel @Inject constructor(
    private val repository: ApiConfigRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel<GeminiConfigUIIntent, GeminiConfigUIState, SnackbarUIEffect>(GeminiConfigUIState()) {

    private val parentModel = AIModel.Gemini

    init {
        viewModelScope.launch {
            val subModels = SubModel.entries.filter { it.parent == parentModel }
            val defaultSubModel = repository.getDefaultSubModelForModel(parentModel).firstOrNull()
            val initialOptionIndex = if (defaultSubModel != null) subModels.indexOf(defaultSubModel).coerceAtLeast(0) else 0

            combine(
                repository.getApiKeyForModel(parentModel),
                repository.getGeminiConfig(),
                settingsRepository.getFloatingWindowQuestionMode(),
                repository.getModelEnabled(parentModel),
                repository.getDefaultSubModelForModel(parentModel)
            ) { apiKey, geminiConfig, questionMode, isEnabled, currentSubModel ->

                val floatingWindowInstruction = repository.getFloatingWindowSystemInstruction(parentModel, questionMode).firstOrNull().orEmpty()
                val optionIndex = subModels.indexOf(currentSubModel).coerceAtLeast(0)

                GeminiConfigUIState(
                    isLoading = false,
                    isModelEnabled = isEnabled,
                    subModels = subModels,
                    optionIndex = optionIndex,
                    apiKey = apiKey,
                    isSaved = uiState.value.isSaved,
                    currentFloatingWindowQuestionMode = questionMode,
                    editableFloatingWindowSystemInstruction = floatingWindowInstruction,
                    geminiConfig = geminiConfig
                )
            }.collectLatest { newState ->
                emitState(newState)
            }
        }
    }

    override suspend fun onEvent(state: GeminiConfigUIState, intent: GeminiConfigUIIntent) {
        when (intent) {
            is GeminiConfigUIIntent.ToggleModelEnabled -> {
                repository.setModelEnabled(parentModel, intent.isEnabled)
            }
            is GeminiConfigUIIntent.UpdateApiKey -> {
                updateState { it.copy(apiKey = intent.apiKey, isSaved = false) }
            }
            is GeminiConfigUIIntent.SaveSettings -> {
                updateState { it.copy(isSaved = true) }
                repository.setApiKeyForModel(parentModel, state.apiKey)
                state.subModels.getOrNull(state.optionIndex)?.let {
                    repository.setDefaultSubModelForModel(parentModel, it)
                }
                repository.setFloatingWindowSystemInstruction(
                    parentModel,
                    state.currentFloatingWindowQuestionMode,
                    state.editableFloatingWindowSystemInstruction
                )
                repository.setGeminiTemperature(state.geminiConfig.temperature)
                repository.setGeminiTopP(state.geminiConfig.topP)
                repository.setGeminiTopK(state.geminiConfig.topK)
                repository.setGeminiResponseMimeType(state.geminiConfig.responseMimeType)
                repository.setGeminiSystemInstruction(state.geminiConfig.systemInstruction)

                emitEffect(SnackbarUIEffect.ShowSnackbar("Settings saved for ${parentModel.name}"))
            }
            is GeminiConfigUIIntent.SelectOption -> {
                updateState { it.copy(optionIndex = intent.index, isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateFloatingWindowSystemInstruction -> {
                updateState { it.copy(editableFloatingWindowSystemInstruction = intent.instruction, isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateGeminiTemperature -> {
                updateState { it.copy(geminiConfig = it.geminiConfig.copy(temperature = intent.temperature), isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateGeminiTopP -> {
                updateState { it.copy(geminiConfig = it.geminiConfig.copy(topP = intent.topP), isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateGeminiTopK -> {
                updateState { it.copy(geminiConfig = it.geminiConfig.copy(topK = intent.topK), isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateGeminiResponseMimeType -> {
                updateState { it.copy(geminiConfig = it.geminiConfig.copy(responseMimeType = intent.mimeType), isSaved = false) }
            }
            is GeminiConfigUIIntent.UpdateGeminiSystemInstruction -> {
                updateState { it.copy(geminiConfig = it.geminiConfig.copy(systemInstruction = intent.instruction), isSaved = false) }
            }
        }
    }
}