package killua.dev.aitalk.ui.viewmodels

import android.util.Log
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
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
) : UIState

sealed interface ApiConfigUIIntent : UIIntent {
    data class LoadAll(val parentModel: AIModel) : ApiConfigUIIntent
    data class UpdateApiKey(val model: AIModel, val apiKey: String) : ApiConfigUIIntent
    data class SaveApiKey(val model: AIModel) : ApiConfigUIIntent
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
                Log.d("ApiConfigViewModel", "Loading all for ${intent.parentModel}")
                val subModels = SubModel.entries.filter { it.parent == intent.parentModel }
                val apiKey = repository.getApiKeyForModel(intent.parentModel).firstOrNull().orEmpty()
                emitState(
                    state.copy(
                        subModels = subModels,
                        apiKey = apiKey,
                        isLoading = false
                    )
                )
            }
            is ApiConfigUIIntent.UpdateApiKey -> {
                updateState { old ->
                    old.copy(apiKey = intent.apiKey, isSaved = false)
                }
            }
            is ApiConfigUIIntent.SaveApiKey -> {
                repository.setApiKeyForModel(intent.model, state.apiKey)
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