package killua.dev.aitalk.repository.impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.api.DeepSeekConfig
import killua.dev.aitalk.api.GeminiConfig
import killua.dev.aitalk.api.GrokConfig
import killua.dev.aitalk.datastore.apiKeyKeyForModel
import killua.dev.aitalk.datastore.readApiKeyForModel
import killua.dev.aitalk.datastore.readDeepSeekSystemInstruction
import killua.dev.aitalk.datastore.readDeepSeekTemperature
import killua.dev.aitalk.datastore.readDefaultSubModelForModel
import killua.dev.aitalk.datastore.readFloatingWindowSystemInstruction
import killua.dev.aitalk.datastore.readGeminiResponseMimeType
import killua.dev.aitalk.datastore.readGeminiSystemInstruction
import killua.dev.aitalk.datastore.readGeminiTemperature
import killua.dev.aitalk.datastore.readGeminiTopK
import killua.dev.aitalk.datastore.readGeminiTopP
import killua.dev.aitalk.datastore.readGrokSystemMessage
import killua.dev.aitalk.datastore.readGrokTemperature
import killua.dev.aitalk.datastore.readModelEnabled
import killua.dev.aitalk.datastore.writeApiKeyForModel
import killua.dev.aitalk.datastore.writeDeepSeekSystemInstruction
import killua.dev.aitalk.datastore.writeDeepSeekTemperature
import killua.dev.aitalk.datastore.writeDefaultSubModelForModel
import killua.dev.aitalk.datastore.writeFloatingWindowSystemInstruction
import killua.dev.aitalk.datastore.writeGeminiResponseMimeType
import killua.dev.aitalk.datastore.writeGeminiSystemInstruction
import killua.dev.aitalk.datastore.writeGeminiTemperature
import killua.dev.aitalk.datastore.writeGeminiTopK
import killua.dev.aitalk.datastore.writeGeminiTopP
import killua.dev.aitalk.datastore.writeGrokSystemMessage
import killua.dev.aitalk.datastore.writeGrokTemperature
import killua.dev.aitalk.datastore.writeModelEnabled
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.repository.ApiConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiConfigRepository {
    override fun getApiKeyForModel(model: AIModel): Flow<String> {
        val key = apiKeyKeyForModel(model)
        Log.d("ApiConfigRepo", "Getting API key for model ${model.name}. Key used: ${key.name}")
        return context.readApiKeyForModel(model)
    }
    override fun getModelEnabled(model: AIModel): Flow<Boolean> =
        context.readModelEnabled(model)

    override fun hasAnyApiKeyBeenSet(): Flow<Boolean> {
        val apiKeyFlows = AIModel.entries.map { getApiKeyForModel(it) }
        return combine(apiKeyFlows) { keys ->
            keys.any { it.isNotBlank() }
        }
    }

    override fun isAnyModelWithKeyEnabled(): Flow<Boolean> {
        val modelConfigFlows = AIModel.entries.map { model ->
            combine(
                getApiKeyForModel(model),
                getModelEnabled(model)
            ) { apiKey, isEnabled ->
                Pair(apiKey, isEnabled)
            }
        }
        return combine(modelConfigFlows) { configs ->
            configs.any { (apiKey, isEnabled) ->
                apiKey.isNotBlank() && isEnabled
            }
        }
    }

    override suspend fun setModelEnabled(model: AIModel, isEnabled: Boolean) {
        context.writeModelEnabled(model, isEnabled)
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
        Log.d("ApiConfigRepo", "Setting default sub model for model ${model.name}. Value: ${subModel.name}")
        context.writeDefaultSubModelForModel(model, subModel)
    }
    override fun getFloatingWindowSystemInstruction(model: AIModel, questionMode: FloatingWindowQuestionMode): Flow<String> {
        return context.readFloatingWindowSystemInstruction(questionMode, model)
    }

    override suspend fun setFloatingWindowSystemInstruction(model: AIModel, questionMode: FloatingWindowQuestionMode, instruction: String) {
        context.writeFloatingWindowSystemInstruction(questionMode, model, instruction)
    }


    //Grok
    override fun getGrokConfig(): Flow<GrokConfig> {
        return combine(
            context.readGrokTemperature(),
            context.readGrokSystemMessage()
        ) { temperature, systemInstruction ->
            GrokConfig(temperature, systemInstruction)
        }
    }

    override suspend fun saveGrokSystemInstruction(message: String) {
        context.writeGrokSystemMessage(message)
    }

    override suspend fun saveGrokTemperature(temperature: Double) {
        context.writeGrokTemperature(temperature)
    }

    //Gemini

    override fun getGeminiConfig(): Flow<GeminiConfig> {
        return combine(
            context.readGeminiTemperature(),
            context.readGeminiTopP(),
            context.readGeminiTopK(),
            context.readGeminiResponseMimeType(),
            context.readGeminiSystemInstruction()
        ) { temperature, topP, topK, responseMimeType, systemInstruction ->
            GeminiConfig(temperature, topP, topK, responseMimeType, systemInstruction)
        }
    }

    override suspend fun setGeminiTemperature(temperature: Double) {
        context.writeGeminiTemperature(temperature)
    }

    override suspend fun setGeminiTopP(topP: Double) {
        context.writeGeminiTopP(topP)
    }

    override suspend fun setGeminiTopK(topK: Int) {
        context.writeGeminiTopK(topK)
    }

    override suspend fun setGeminiResponseMimeType(mimeType: String) {
        context.writeGeminiResponseMimeType(mimeType)
    }

    override suspend fun setGeminiSystemInstruction(instruction: String) {
        context.writeGeminiSystemInstruction(instruction)
    }

    //Deepseek
    override fun getDeepSeekConfig(): Flow<DeepSeekConfig> {
        return combine(
            context.readDeepSeekTemperature(),
            context.readDeepSeekSystemInstruction()
        ) { temperature, systemInstruction ->
            DeepSeekConfig(temperature, systemInstruction)
        }
    }

    override suspend fun setDeepSeekTemperature(temperature: Double) {
        context.writeDeepSeekTemperature(temperature)
    }

    override suspend fun setDeepSeekSystemInstruction(instruction: String) {
        context.writeDeepSeekSystemInstruction(instruction)
    }
}