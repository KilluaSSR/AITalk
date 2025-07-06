package killua.dev.aitalk.repository.impl

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.ExtraInformation
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
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val networkRepo: AiNetworkRepository
) : AiRepository {
    override fun fetchAiResponses(
        query: String,
        subModelMap: Map<AIModel, SubModel>,
        extraInformation: ExtraInformation
    ): Flow<Pair<AIModel, AIResponseState>> = channelFlow {
        for ((model, subModel) in subModelMap) {
            launch {
                networkRepo.fetchResponseStream(query, subModel, extraInformation).collect { state->
                    send(model to state)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}