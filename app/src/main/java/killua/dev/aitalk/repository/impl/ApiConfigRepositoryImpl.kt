package killua.dev.aitalk.repository.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.datastore.readApiKeyForSubModel
import killua.dev.aitalk.datastore.writeApiKeyForSubModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApiConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiConfigRepository {
    override fun getApiKeyForSubModel(subModel: SubModel): Flow<String> =
        context.readApiKeyForSubModel(subModel)

    override suspend fun setApiKeyForSubModel(subModel: SubModel, apiKey: String) {
        context.writeApiKeyForSubModel(subModel, apiKey)
    }
}