package killua.dev.aitalk.repository.impl

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.AiRepository
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val networkRepo: AiNetworkRepository
) : AiRepository {
    override fun fetchAiResponses(query: String,  subModelMap: Map<AIModel, SubModel>): Flow<Pair<AIModel, AIResponseState>> = channelFlow {
        for ((model, subModel) in subModelMap) {
            launch {
                val result = networkRepo.fetchResponse(query, subModel)
                send(model to result)
            }
        }
    }.flowOn(Dispatchers.IO)
}