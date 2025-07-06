package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.api.GrokConfig
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

data class GrokConfigUIState(
    val subModels: List<SubModel> = emptyList(),
    val optionIndex: Int = 0,
    val isModelEnabled: Boolean = true,
    val apiKey: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val currentFloatingWindowQuestionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain,
    val editableFloatingWindowSystemInstruction: String = "",
    val grokConfig: GrokConfig = GrokConfig(),
) : UIState

sealed interface GrokConfigUIIntent : UIIntent {
    data class UpdateApiKey(val apiKey: String) : GrokConfigUIIntent
    data object SaveSettings : GrokConfigUIIntent
    data class SelectOption(val index: Int) : GrokConfigUIIntent
    data class ToggleModelEnabled(val isEnabled: Boolean) : GrokConfigUIIntent
    data class UpdateFloatingWindowSystemInstruction(val instruction: String) : GrokConfigUIIntent
    data class UpdateGrokSystemInstruction(val instruction: String) : GrokConfigUIIntent
    data class UpdateGrokTemperature(val temperature: Double) : GrokConfigUIIntent
}

@HiltViewModel
class GrokConfigViewModel @Inject constructor(
    private val repository: ApiConfigRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel<GrokConfigUIIntent, GrokConfigUIState, SnackbarUIEffect>(GrokConfigUIState()) {

    private val parentModel = AIModel.Grok

    init {
        viewModelScope.launch {
            val subModels = SubModel.entries.filter { it.parent == parentModel }
            val defaultSubModel = repository.getDefaultSubModelForModel(parentModel).firstOrNull()
            val initialOptionIndex = if (defaultSubModel != null) subModels.indexOf(defaultSubModel).coerceAtLeast(0) else 0

            combine(
                repository.getApiKeyForModel(parentModel),
                repository.getGrokConfig(),
                settingsRepository.getFloatingWindowQuestionMode(),
                repository.getModelEnabled(parentModel),
                repository.getDefaultSubModelForModel(parentModel)
            ) { apiKey, grokConfig, questionMode, isEnabled, currentSubModel ->

                val floatingWindowInstruction = repository.getFloatingWindowSystemInstruction(parentModel, questionMode).firstOrNull().orEmpty()
                val optionIndex = subModels.indexOf(currentSubModel).coerceAtLeast(0)

                GrokConfigUIState(
                    isLoading = false,
                    isModelEnabled = isEnabled,
                    subModels = subModels,
                    optionIndex = optionIndex,
                    apiKey = apiKey,
                    isSaved = uiState.value.isSaved,
                    currentFloatingWindowQuestionMode = questionMode,
                    editableFloatingWindowSystemInstruction = floatingWindowInstruction,
                    grokConfig = grokConfig
                )
            }.collectLatest { newState ->
                emitState(newState)
            }
        }
    }

    override suspend fun onEvent(state: GrokConfigUIState, intent: GrokConfigUIIntent) {
        when (intent) {

            is GrokConfigUIIntent.UpdateApiKey -> {
                updateState { it.copy(apiKey = intent.apiKey, isSaved = false) }
            }
            is GrokConfigUIIntent.SaveSettings -> {
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
                repository.saveGrokSystemInstruction(state.grokConfig.systemInstruction)
                repository.saveGrokTemperature(state.grokConfig.temperature)

                emitEffect(ShowSnackbar("Settings saved for ${parentModel.name}"))
            }
            is GrokConfigUIIntent.SelectOption -> {
                updateState { it.copy(optionIndex = intent.index, isSaved = false) }
            }
            is GrokConfigUIIntent.UpdateFloatingWindowSystemInstruction -> {
                updateState { it.copy(editableFloatingWindowSystemInstruction = intent.instruction, isSaved = false) }
            }
            is GrokConfigUIIntent.UpdateGrokSystemInstruction -> {
                updateState { it.copy(grokConfig = it.grokConfig.copy(systemInstruction = intent.instruction), isSaved = false) }
            }
            is GrokConfigUIIntent.UpdateGrokTemperature -> {
                updateState { it.copy(grokConfig = it.grokConfig.copy(temperature = intent.temperature), isSaved = false) }
            }

            is GrokConfigUIIntent.ToggleModelEnabled -> {
                repository.setModelEnabled(parentModel, intent.isEnabled)
            }
        }
    }
}