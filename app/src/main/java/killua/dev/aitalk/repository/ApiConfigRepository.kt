package killua.dev.aitalk.repository

import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow

interface ApiConfigRepository {
    fun getApiKeyForModel(model: AIModel): Flow<String>
    suspend fun setApiKeyForModel(model: AIModel, apiKey: String)

    fun getDefaultSubModelForModel(model: AIModel): Flow<SubModel?>
    suspend fun setDefaultSubModelForModel(model: AIModel, subModel: SubModel)

    //Grok
    fun getGrokSystemMessage(): Flow<String>
    suspend fun saveGrokSystemMessage(message: String)

    fun getGrokTemperature(): Flow<Double>
    suspend fun saveGrokTemperature(temperature: Double)
}
