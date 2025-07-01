package killua.dev.aitalk.repository

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow

interface ApiConfigRepository {
    fun getApiKeyForModel(model: AIModel): Flow<String>
    suspend fun setApiKeyForModel(model: AIModel, apiKey: String)
}
