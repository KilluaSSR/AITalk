package killua.dev.aitalk.repository.impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.datastore.apiKeyKeyForModel
import killua.dev.aitalk.datastore.readApiKeyForModel
import killua.dev.aitalk.datastore.readDefaultSubModelForModel
import killua.dev.aitalk.datastore.writeApiKeyForModel
import killua.dev.aitalk.datastore.writeDefaultSubModelForModel
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ApiConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiConfigRepository {
    override fun getApiKeyForModel(model: AIModel): Flow<String> {
        val key = apiKeyKeyForModel(model)
        Log.d("ApiConfigRepo", "Getting API key for model ${model.name}. Key used: ${key.name}")
        return context.readApiKeyForModel(model)
    }

    override suspend fun setApiKeyForModel(model: AIModel, apiKey: String) {
        val key = apiKeyKeyForModel(model)
        Log.d("ApiConfigRepo", "Setting API key for model ${model.name}. Key used: ${key.name}. Value: $apiKey")
        context.writeApiKeyForModel(model, apiKey)
    }
    override fun getDefaultSubModelForModel(model: AIModel): Flow<SubModel?> =
        context.readDefaultSubModelForModel(model)
            .map { name -> SubModel.entries.find { it.name == name } }

    override suspend fun setDefaultSubModelForModel(model: AIModel, subModel: SubModel) {
        context.writeDefaultSubModelForModel(model, subModel)
    }
}