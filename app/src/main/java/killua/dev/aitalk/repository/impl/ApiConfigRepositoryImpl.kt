package killua.dev.aitalk.repository.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.datastore.readApiKeyForModel
import killua.dev.aitalk.datastore.writeApiKeyForModel
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.ApiConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApiConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiConfigRepository {
    override fun getApiKeyForModel(model: AIModel): Flow<String> =
        context.readApiKeyForModel(model)

    override suspend fun setApiKeyForModel(model: AIModel, apiKey: String) {
        context.writeApiKeyForModel(model, apiKey)
    }
}