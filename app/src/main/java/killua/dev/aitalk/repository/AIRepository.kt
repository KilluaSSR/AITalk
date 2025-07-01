package killua.dev.aitalk.repository

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun fetchAiResponses(query: String, subModelMap: Map<AIModel, SubModel>): Flow<Pair<AIModel, AIResponseState>>
}