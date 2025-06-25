package killua.dev.aitalk.ui.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.viewmodels.base.BaseViewModel
import killua.dev.aitalk.ui.viewmodels.base.UIIntent
import killua.dev.aitalk.ui.viewmodels.base.UIState
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ApiConfigUIState(
    val parentModel: AIModel,
    val subModels: List<SubModel>,
    val optionIndex: Int = 0,
    val apiKeys: Map<SubModel, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
) : UIState

sealed interface ApiConfigUIIntent : UIIntent {
    data class LoadAll(val parentModel: AIModel) : ApiConfigUIIntent
    data class UpdateApiKey(val subModel: SubModel, val apiKey: String) : ApiConfigUIIntent
    data class SaveApiKey(val subModel: SubModel) : ApiConfigUIIntent
    data class SelectOption(val index: Int) : ApiConfigUIIntent
    data object ClearError : ApiConfigUIIntent
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
                emitState(state.copy(isLoading = true, parentModel = intent.parentModel))
                val subModels = SubModel.entries.filter { it.parent == intent.parentModel }
                val apiKeys = mutableMapOf<SubModel, String>()
                for (sub in subModels) {
                    repository.getApiKeyForSubModel(sub).firstOrNull()?.let { key ->
                        apiKeys[sub] = key
                    }
                }
                emitState(
                    state.copy(
                        subModels = subModels,
                        apiKeys = apiKeys,
                        isLoading = false
                    )
                )
            }
            is ApiConfigUIIntent.UpdateApiKey -> {
                updateState { old ->
                    old.copy(apiKeys = old.apiKeys + (intent.subModel to intent.apiKey), isSaved = false)
                }
            }
            is ApiConfigUIIntent.SaveApiKey -> {
                emitState(state.copy(isLoading = true))
                val apiKey = state.apiKeys[intent.subModel] ?: ""
                repository.setApiKeyForSubModel(intent.subModel, apiKey)
                updateState { it.copy(isLoading = false, isSaved = true) }
            }
            ApiConfigUIIntent.ClearError -> {
                updateState { it.copy(errorMessage = null) }
            }
            is ApiConfigUIIntent.SelectOption -> {
                emitState(state.copy(optionIndex = intent.index))
            }
        }
    }
}