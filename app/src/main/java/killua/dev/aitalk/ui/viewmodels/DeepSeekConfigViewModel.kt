package killua.dev.aitalk.ui.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.api.DeepSeekConfig
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.SnackbarUIEffect.*
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.indexOf

data class DeepSeekConfigUIState(
    val subModels: List<SubModel> = emptyList(),
    val optionIndex: Int = 0,
    val apiKey: String = "",
    val isModelEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val currentFloatingWindowQuestionMode: FloatingWindowQuestionMode = FloatingWindowQuestionMode.isThatTrueWithExplain,
    val editableFloatingWindowSystemInstruction: String = "",
    val deepSeekConfig: DeepSeekConfig = DeepSeekConfig(),
) : UIState

sealed interface DeepSeekConfigUIIntent : UIIntent {
    data class UpdateApiKey(val apiKey: String) : DeepSeekConfigUIIntent
    data object SaveSettings : DeepSeekConfigUIIntent
    data class SelectOption(val index: Int) : DeepSeekConfigUIIntent
    data class ToggleModelEnabled(val isEnabled: Boolean) : DeepSeekConfigUIIntent
    data class UpdateFloatingWindowSystemInstruction(val instruction: String) : DeepSeekConfigUIIntent
    data class UpdateDeepSeekTemperature(val temperature: Double) : DeepSeekConfigUIIntent
    data class UpdateDeepSeekSystemInstruction(val instruction: String) : DeepSeekConfigUIIntent
}

@HiltViewModel
class DeepSeekConfigViewModel @Inject constructor(
    private val repository: ApiConfigRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel<DeepSeekConfigUIIntent, DeepSeekConfigUIState, SnackbarUIEffect>(DeepSeekConfigUIState()) {

    private val parentModel = AIModel.DeepSeek

    init {
        viewModelScope.launch {
            val subModels = SubModel.entries.filter { it.parent == parentModel }
            val defaultSubModel = repository.getDefaultSubModelForModel(parentModel).firstOrNull()
            val initialOptionIndex = if (defaultSubModel != null) subModels.indexOf(defaultSubModel).coerceAtLeast(0) else 0

            combine(
                repository.getApiKeyForModel(parentModel),
                repository.getDeepSeekConfig(),
                settingsRepository.getFloatingWindowQuestionMode(),
                repository.getModelEnabled(parentModel),
                repository.getDefaultSubModelForModel(parentModel)
            ) { apiKey, deepSeekConfig, questionMode, isEnabled, currentSubModel ->

                val floatingWindowInstruction = repository.getFloatingWindowSystemInstruction(parentModel, questionMode).firstOrNull().orEmpty()
                val optionIndex = subModels.indexOf(currentSubModel).coerceAtLeast(0)

                DeepSeekConfigUIState(
                    isLoading = false,
                    isModelEnabled = isEnabled,
                    subModels = subModels,
                    optionIndex = optionIndex,
                    apiKey = apiKey,
                    isSaved = uiState.value.isSaved,
                    currentFloatingWindowQuestionMode = questionMode,
                    editableFloatingWindowSystemInstruction = floatingWindowInstruction,
                    deepSeekConfig = deepSeekConfig
                )
            }.collectLatest { newState ->
                emitState(newState)
            }
        }
    }

    override suspend fun onEvent(state: DeepSeekConfigUIState, intent: DeepSeekConfigUIIntent) {
        when (intent) {

            is DeepSeekConfigUIIntent.UpdateApiKey -> {
                updateState { it.copy(apiKey = intent.apiKey, isSaved = false) }
            }
            is DeepSeekConfigUIIntent.SaveSettings -> {
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
                repository.setDeepSeekTemperature(state.deepSeekConfig.temperature)
                repository.setDeepSeekSystemInstruction(state.deepSeekConfig.systemInstruction)

                emitEffect(ShowSnackbar("Settings saved for ${parentModel.name}"))
            }
            is DeepSeekConfigUIIntent.SelectOption -> {
                updateState { it.copy(optionIndex = intent.index, isSaved = false) }
            }
            is DeepSeekConfigUIIntent.UpdateFloatingWindowSystemInstruction -> {
                updateState { it.copy(editableFloatingWindowSystemInstruction = intent.instruction, isSaved = false) }
            }
            is DeepSeekConfigUIIntent.UpdateDeepSeekTemperature -> {
                updateState { it.copy(deepSeekConfig = it.deepSeekConfig.copy(temperature = intent.temperature), isSaved = false) }
            }
            is DeepSeekConfigUIIntent.UpdateDeepSeekSystemInstruction -> {
                updateState { it.copy(deepSeekConfig = it.deepSeekConfig.copy(systemInstruction = intent.instruction), isSaved = false) }
            }

            is DeepSeekConfigUIIntent.ToggleModelEnabled ->{
                repository.setModelEnabled(parentModel, intent.isEnabled)
            }
        }
    }
}