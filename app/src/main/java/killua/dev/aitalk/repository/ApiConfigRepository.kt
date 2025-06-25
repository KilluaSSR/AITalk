package killua.dev.aitalk.repository

import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow

interface ApiConfigRepository {
    fun getApiKeyForSubModel(subModel: SubModel): Flow<String>
    suspend fun setApiKeyForSubModel(subModel: SubModel, apiKey: String)
}
