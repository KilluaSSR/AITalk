package killua.dev.aitalk.repository

import killua.dev.aitalk.models.ExtraInformation
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.flow.Flow

interface AiNetworkRepository {

    fun fetchResponseStream(
        query: String,
        subModel: SubModel,
        extraInformation: ExtraInformation
    ): Flow<AIResponseState>
}