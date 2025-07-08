package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.api.OpenAIConfig
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


data class OpenAIConfigUIState(
    val subModels: List<SubModel> = emptyList(),
    val optionIndex: Int = 0,
    val apiKey: String = "",
    val isModelEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val currentFloatingWindowQuestionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain,
    val editableFloatingWindowSystemInstruction: String = "",
    val openAIConfig: OpenAIConfig = OpenAIConfig(),
) : UIState

sealed interface OpenAIConfigUIIntent : UIIntent {
    data class UpdateApiKey(val apiKey: String) : OpenAIConfigUIIntent
    data object SaveSettings : OpenAIConfigUIIntent
    data class SelectOption(val index: Int) : OpenAIConfigUIIntent
    data class ToggleModelEnabled(val isEnabled: Boolean) : OpenAIConfigUIIntent
    data class UpdateFloatingWindowSystemInstruction(val instruction: String) : OpenAIConfigUIIntent
    data class UpdateOpenAITemperature(val temperature: Double) : OpenAIConfigUIIntent
    data class UpdateOpenAISystemInstruction(val instruction: String) : OpenAIConfigUIIntent
}

@HiltViewModel
class OpenAIConfigViewModel @Inject constructor(
    private val repository: ApiConfigRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel<OpenAIConfigUIIntent, OpenAIConfigUIState, SnackbarUIEffect>(OpenAIConfigUIState()) {

    private val parentModel = AIModel.ChatGPT

    init {
        viewModelScope.launch {
            val subModels = SubModel.entries.filter { it.parent == parentModel }
            val defaultSubModel = repository.getDefaultSubModelForModel(parentModel).firstOrNull()
            val initialOptionIndex = if (defaultSubModel != null) subModels.indexOf(defaultSubModel).coerceAtLeast(0) else 0

            combine(
                repository.getApiKeyForModel(parentModel),
                repository.getOpenAIConfig(),
                settingsRepository.getFloatingWindowQuestionMode(),
                repository.getModelEnabled(parentModel),
                repository.getDefaultSubModelForModel(parentModel)
            ) { apiKey, openAIConfig, questionMode, isEnabled, currentSubModel ->

                val floatingWindowInstruction = repository.getFloatingWindowSystemInstruction(parentModel, questionMode).firstOrNull().orEmpty()
                val optionIndex = subModels.indexOf(currentSubModel).coerceAtLeast(0)

                OpenAIConfigUIState(
                    isLoading = false,
                    isModelEnabled = isEnabled,
                    subModels = subModels,
                    optionIndex = optionIndex,
                    apiKey = apiKey,
                    isSaved = uiState.value.isSaved,
                    currentFloatingWindowQuestionMode = questionMode,
                    editableFloatingWindowSystemInstruction = floatingWindowInstruction,
                    openAIConfig = openAIConfig
                )
            }.collectLatest { newState ->
                emitState(newState)
            }
        }
    }

    override suspend fun onEvent(state: OpenAIConfigUIState, intent: OpenAIConfigUIIntent) {
        when (intent) {

            is OpenAIConfigUIIntent.UpdateApiKey -> {
                updateState { it.copy(apiKey = intent.apiKey, isSaved = false) }
            }
            is OpenAIConfigUIIntent.SaveSettings -> {
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
                repository.setOpenAITemperature(state.openAIConfig.temperature)
                repository.setOpenAISystemInstruction(state.openAIConfig.systemInstruction)

                emitEffect(ShowSnackbar("Settings saved for ${parentModel.name}"))
            }
            is OpenAIConfigUIIntent.SelectOption -> {
                updateState { it.copy(optionIndex = intent.index, isSaved = false) }
            }
            is OpenAIConfigUIIntent.UpdateFloatingWindowSystemInstruction -> {
                updateState { it.copy(editableFloatingWindowSystemInstruction = intent.instruction, isSaved = false) }
            }
            is OpenAIConfigUIIntent.UpdateOpenAITemperature -> {
                updateState { it.copy(openAIConfig = it.openAIConfig.copy(temperature = intent.temperature), isSaved = false) }
            }
            is OpenAIConfigUIIntent.UpdateOpenAISystemInstruction -> {
                updateState { it.copy(openAIConfig = it.openAIConfig.copy(systemInstruction = intent.instruction), isSaved = false) }
            }

            is OpenAIConfigUIIntent.ToggleModelEnabled ->{
                repository.setModelEnabled(parentModel, intent.isEnabled)
            }
        }
    }
}