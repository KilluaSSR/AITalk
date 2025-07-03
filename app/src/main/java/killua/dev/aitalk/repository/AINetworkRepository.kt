package killua.dev.aitalk.repository

import killua.dev.aitalk.models.ExtraInformation
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.states.AIResponseState

interface AiNetworkRepository {
    suspend fun fetchResponse(
        query: String,
        subModel: SubModel,
        extraInformation: ExtraInformation
    ): AIResponseState
}