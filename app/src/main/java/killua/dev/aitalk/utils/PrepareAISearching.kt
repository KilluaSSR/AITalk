package killua.dev.aitalk.utils

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import kotlinx.coroutines.flow.firstOrNull

data class AiSearchData(
    val filteredSubModelMap: Map<AIModel, SubModel>,
    val apiKeyMap: Map<AIModel, String>,
    val initialResponses: Map<AIModel, AIResponseState>
)

suspend fun prepareAiSearchData(
    apiConfigRepository: ApiConfigRepository,
    currentSubModelMap: Map<AIModel, SubModel>? = null
): AiSearchData {
    val subModelMap: Map<AIModel, SubModel> = AIModel.entries.associateWith { model ->
        currentSubModelMap?.get(model)
            ?: apiConfigRepository.getDefaultSubModelForModel(model).firstOrNull()
            ?: SubModel.entries.first { it.parent == model }
    }

    val apiKeyMap: Map<AIModel, String> = AIModel.entries.associateWith { model ->
        apiConfigRepository.getApiKeyForModel(model).firstOrNull().orEmpty()
    }

    val turnedOnMap: Map<AIModel, Boolean> = AIModel.entries.associateWith { model ->
        apiConfigRepository.getModelEnabled(model).firstOrNull() == true
    }

    val filteredSubModelMap: Map<AIModel, SubModel> = subModelMap.filter { (model, _) ->
        (apiKeyMap[model]?.isNotBlank() == true) && turnedOnMap[model] == true
    }

    val initialResponses: Map<AIModel, AIResponseState> = filteredSubModelMap.keys.associateWith {
        AIResponseState(status = ResponseStatus.Loading)
    }

    return AiSearchData(filteredSubModelMap, apiKeyMap, initialResponses)
}