package killua.dev.aitalk.ui.viewmodels

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.api.configuration.DeepSeekConfig
import killua.dev.aitalk.api.configuration.GeminiConfig
import killua.dev.aitalk.api.configuration.GrokConfig
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ApiConfigUIState(
    val parentModel: AIModel,
    val subModels: List<SubModel>,
    val optionIndex: Int = 0,
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val grokConfig: GrokConfig = GrokConfig(),
    val geminiConfig: GeminiConfig = GeminiConfig(),
    val deepSeekConfig: DeepSeekConfig = DeepSeekConfig(),
) : UIState

sealed interface ApiConfigUIIntent : UIIntent {
    data class LoadAll(val parentModel: AIModel) : ApiConfigUIIntent
    data class UpdateApiKey(val model: AIModel, val apiKey: String) : ApiConfigUIIntent
    data class SaveApiSettings(val model: AIModel) : ApiConfigUIIntent
    data class SelectOption(val index: Int) : ApiConfigUIIntent
    data object ClearError : ApiConfigUIIntent

    data class UpdateGrokSystemInstruction(val instruction: String) : ApiConfigUIIntent
    data class UpdateGrokTemperature(val temperature: Double) : ApiConfigUIIntent

    data class UpdateGeminiTemperature(val temperature: Double) : ApiConfigUIIntent
    data class UpdateGeminiTopP(val topP: Double) : ApiConfigUIIntent
    data class UpdateGeminiTopK(val topK: Int) : ApiConfigUIIntent
    data class UpdateGeminiResponseMimeType(val mimeType: String) : ApiConfigUIIntent
    data class UpdateGeminiSystemInstruction(val instruction: String) : ApiConfigUIIntent
    data class UpdateDeepSeekTemperature(val temperature: Double) : ApiConfigUIIntent
    data class UpdateDeepSeekSystemInstruction(val instruction: String) : ApiConfigUIIntent
}

@HiltViewModel
class ApiConfigViewModel @Inject constructor(
    private val repository: ApiConfigRepository
) : BaseViewModel<ApiConfigUIIntent, ApiConfigUIState, SnackbarUIEffect>(
    ApiConfigUIState(parentModel = AIModel.ChatGPT, subModels = emptyList())
) {
    override suspend fun onEvent(state: ApiConfigUIState, intent: ApiConfigUIIntent) {
        when (intent) {
            is ApiConfigUIIntent.LoadAll -> {
                Log.d("ApiConfigViewModel", "Loading all for ${intent.parentModel}")
                emitState(state.copy(isLoading = true, parentModel = intent.parentModel))

                val subModels = SubModel.entries.filter { it.parent == intent.parentModel }
                val apiKey = repository.getApiKeyForModel(intent.parentModel).firstOrNull().orEmpty()

                val defaultSubModelFlow = repository.getDefaultSubModelForModel(intent.parentModel)
                val defaultSubModel = defaultSubModelFlow.firstOrNull()

                val optionIndex = if (defaultSubModel != null) {
                    subModels.indexOf(defaultSubModel).coerceAtLeast(0)
                } else {
                    0
                }

                // 加载 Grok 或 Gemini 特定的配置
                when (intent.parentModel) {
                    AIModel.Grok -> {
                        repository.getGrokConfig().collectLatest { grokConfig ->
                            emitState(
                                state.copy(
                                    subModels = subModels,
                                    apiKey = apiKey,
                                    grokConfig = grokConfig,
                                    optionIndex = optionIndex,
                                    isLoading = false
                                )
                            )
                        }
                    }
                    AIModel.Gemini -> {
                        repository.getGeminiConfig().collectLatest { geminiConfig ->
                            emitState(
                                state.copy(
                                    subModels = subModels,
                                    apiKey = apiKey,
                                    geminiConfig = geminiConfig,
                                    optionIndex = optionIndex,
                                    isLoading = false
                                )
                            )
                        }
                    }
                    AIModel.DeepSeek -> {
                        repository.getDeepSeekConfig().collectLatest { deepSeekConfig ->
                            emitState(
                                state.copy(
                                    subModels = subModels,
                                    apiKey = apiKey,
                                    deepSeekConfig = deepSeekConfig,
                                    optionIndex = optionIndex,
                                    isLoading = false
                                )
                            )
                        }
                    }
                    else -> {
                        emitState(
                            state.copy(
                                subModels = subModels,
                                apiKey = apiKey,
                                optionIndex = optionIndex,
                                isLoading = false
                            )
                        )
                    }
                }
            }

            is ApiConfigUIIntent.UpdateApiKey -> {
                updateState { old ->
                    old.copy(apiKey = intent.apiKey, isSaved = false)
                }
            }

            is ApiConfigUIIntent.SaveApiSettings -> {
                Log.d("ApiConfigViewModel", "Saving all settings for ${intent.model}")
                updateState { it.copy(isSaved = true) }


                repository.setApiKeyForModel(intent.model, state.apiKey)


                val selectedSubModel = state.subModels.getOrNull(state.optionIndex)
                selectedSubModel?.let {
                    repository.setDefaultSubModelForModel(intent.model, it)
                }


                when (intent.model) {
                    AIModel.Grok -> {
                        repository.saveGrokSystemInstruction(state.grokConfig.systemInstruction)
                        repository.saveGrokTemperature(state.grokConfig.temperature)
                    }
                    AIModel.Gemini -> {
                        repository.setGeminiTemperature(state.geminiConfig.temperature)
                        repository.setGeminiTopP(state.geminiConfig.topP)
                        repository.setGeminiTopK(state.geminiConfig.topK)
                        repository.setGeminiResponseMimeType(state.geminiConfig.responseMimeType)
                        repository.setGeminiSystemInstruction(state.geminiConfig.systemInstruction)
                    }
                    AIModel.DeepSeek -> {
                        repository.setDeepSeekTemperature(state.deepSeekConfig.temperature)
                        repository.setDeepSeekSystemInstruction(state.deepSeekConfig.systemInstruction)
                    }
                    else -> {

                    }
                }

                emitEffect(SnackbarUIEffect.ShowSnackbar("Settings saved for ${intent.model.name}"))
            }

            ApiConfigUIIntent.ClearError -> {
                updateState { it.copy(errorMessage = null) }
            }

            is ApiConfigUIIntent.SelectOption -> {
                updateState { it.copy(optionIndex = intent.index, isSaved = false) }
            }


            is ApiConfigUIIntent.UpdateGrokSystemInstruction -> {
                updateState { old ->
                    old.copy(grokConfig = old.grokConfig.copy(systemInstruction = intent.instruction), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateGrokTemperature -> {
                updateState { old ->
                    old.copy(grokConfig = old.grokConfig.copy(temperature = intent.temperature), isSaved = false)
                }
            }


            is ApiConfigUIIntent.UpdateGeminiTemperature -> {
                updateState { old ->
                    old.copy(geminiConfig = old.geminiConfig.copy(temperature = intent.temperature), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateGeminiTopP -> {
                updateState { old ->
                    old.copy(geminiConfig = old.geminiConfig.copy(topP = intent.topP), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateGeminiTopK -> {
                updateState { old ->
                    old.copy(geminiConfig = old.geminiConfig.copy(topK = intent.topK), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateGeminiResponseMimeType -> {
                updateState { old ->
                    old.copy(geminiConfig = old.geminiConfig.copy(responseMimeType = intent.mimeType), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateGeminiSystemInstruction -> {
                updateState { old ->
                    old.copy(geminiConfig = old.geminiConfig.copy(systemInstruction = intent.instruction), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateDeepSeekTemperature -> {
                updateState { old ->
                    old.copy(deepSeekConfig = old.deepSeekConfig.copy(temperature = intent.temperature), isSaved = false)
                }
            }
            is ApiConfigUIIntent.UpdateDeepSeekSystemInstruction -> {
                updateState { old ->
                    old.copy(deepSeekConfig = old.deepSeekConfig.copy(systemInstruction = intent.instruction), isSaved = false)
                }
            }
        }
    }
}