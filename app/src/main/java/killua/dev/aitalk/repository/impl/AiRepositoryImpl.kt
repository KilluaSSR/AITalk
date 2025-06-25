package killua.dev.aitalk.repository.impl

import killua.dev.aitalk.models.AIModel
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
    override fun fetchAiResponses(query: String): Flow<Pair<AIModel, AIResponseState>> = channelFlow {
        for (model in AIModel.entries) {
            launch {
                val result = networkRepo.fetchResponse(model, query)
                send(model to result)
            }
        }
    }.flowOn(Dispatchers.IO)
}